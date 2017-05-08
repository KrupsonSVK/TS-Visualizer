package view.graphTabs;

import app.streamAnalyzer.TimestampParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Stream;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import static model.config.MPEG.*;

import static app.Main.localization;
import static model.MapHandler.*;
import static model.config.Config.*;

public class BitrateTab extends TimestampParser implements Graph{
    private Scene scene;
    public Tab  tab;

    private CheckBox groupByCheckBox;
    private Map PIDmap;
    private Map deltaServiceBitrateMap;
    private Map deltaPIDBitrateMap;
    public BitrateTab(){
        tab = new Tab(localization.getBitrateTabText());
    }


    public void drawGraph(Stream stream) {

        Map sortedPIDBitrateMap = sortHashMapByKey(stream.getTables().getIndexSnapshotMap());
        Map sortedServiceBitrateMap = groupByService(stream.getTables().getPMTmap(),sortedPIDBitrateMap);

        deltaPIDBitrateMap = createDeltaBitrateMap(sortedPIDBitrateMap);
        deltaServiceBitrateMap = createDeltaBitrateMap(sortedServiceBitrateMap);

        PIDmap = sortHashMapByKey(stream.getTables().getPIDmap());

        long startTimeStamp = 0; // (long) getFirstItem( stream.getTables().getPCRsnapshotMap()).getKey(); //TODO start and end time of broadcast
        long endTimeStamp = stream.getDuration(); //(long) getLastItem( stream.getTables().getPCRsnapshotMap()).getKey();
        long duration = endTimeStamp - startTimeStamp;
        long tickInterval = duration / deltaPIDBitrateMap.size();

        final NumberAxis xAxis = new NumberAxis(0,deltaPIDBitrateMap.size()-1,tickUnit);
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setTickLabelFormatter(
                new NumberAxis.DefaultFormatter(yAxis) {
                    @Override
                    public String toString(Number object) {
                        long timestamp = startTimeStamp + (tickInterval * object.longValue());
                        String out = parseTimestamp(timestamp);
                        return String.format("%s",out);
                    }
                });
        yAxis.setTickLabelFormatter(
                new NumberAxis.DefaultFormatter(yAxis) {
                    @Override
                    public String toString(Number object) {
                        Double bitrate = (object.doubleValue() * deltaPIDBitrateMap.size() * tsPacketSize * byteBinaryLength / 1024. ) / duration;
                        return String.format("%1$,.2f MBit/s",bitrate);
                    }
                });

        xAxis.setLabel(localization.getTimeText());
        yAxis.setLabel(localization.getBitrateText());

        StackedAreaChart stackedAreaChart = new StackedAreaChart<>(xAxis, yAxis);
        stackedAreaChart.setTitle(localization.getMultiplexBitrateText());
        stackedAreaChart.setCreateSymbols(false);
        stackedAreaChart.setPadding(chartInsets);
        stackedAreaChart.setPrefHeight(scene.getHeight());
        stackedAreaChart.setAnimated(false);

        groupByCheckBox = new CheckBox(localization.getGroupProgramsText());
        HBox checkHBox = new HBox(groupByCheckBox);
        checkHBox.setAlignment(Pos.CENTER);
        checkHBox.setSpacing(10);
        checkHBox.setPadding(chartHBoxInsets);

        addListenersAndHandlers(stackedAreaChart);
        groupByCheckBox.fire();

        tab.setContent(new VBox(stackedAreaChart,checkHBox));
    }


    private Map groupByService(Map<Integer,Integer> PMTmap, Map<Integer,Map<Integer,Integer>> sortedPIDdeltaBitrateMaps) {

        Map<Integer, Map<Integer, Integer>> sortedServiceDeltaBitrateMaps = new LinkedHashMap();
        for (Map.Entry<Integer, Map<Integer, Integer>> PIDdeltaBitrateMap : sortedPIDdeltaBitrateMaps.entrySet()) {

            Map<Integer, Integer> serviceDeltaBitrateMap = new LinkedHashMap();
            for (Map.Entry<Integer, Integer> PIDentry : PIDdeltaBitrateMap.getValue().entrySet()) {

                Integer deltaBitrate = PIDentry.getValue();
                Integer PID = PIDentry.getKey();
                Integer serviceNumber = PMTmap.get(PID);
                if (serviceNumber != null) {
                    serviceDeltaBitrateMap = updateMap(serviceDeltaBitrateMap, serviceNumber, deltaBitrate);
                }
                else {
                    serviceDeltaBitrateMap = updateMap(serviceDeltaBitrateMap, PID, deltaBitrate);
                }
            }
            sortedServiceDeltaBitrateMaps.put(PIDdeltaBitrateMap.getKey(), serviceDeltaBitrateMap);
        }
        return sortedServiceDeltaBitrateMaps;
    }


    /**
     * Metóda zostavenia dát pre vytvorenie kumulovaného plošného grafu prenosovej rýchlosti v čase
     *
     * @param map hashmapa ako zoznam PIDov, alebo čísel služieb, prípadne ES tokov
     * @param bitrateMap dvojitá hashmapa s prenosovými rýchlosťami jednotlivých PIDov v čase
     * @return kolekciu grafových dát obsahujúcu série tvorené súradnicami X a Y
     */
    private Collection createBitrateChart(Map<Integer,Integer> map, Map<Integer,Map> bitrateMap) {

        ObservableList<XYChart.Series> chartData = FXCollections.observableArrayList(); //inicialzácia kolekcie grafových dát

        for (Integer PID : map.keySet()) { //cyklus prechádza všetky PID resp. služby, pre ktoré vytvára plošné krivky
            final XYChart.Series series = new XYChart.Series<>(); //séria bodov tvoriaca plošnú krivku aktuálneho PIDu
            series.setName("PID: " + String.format("0x%04X", PID & 0xFFFF)  + " (" + PID + ")"); //názov série daný PIDom
            //cyklus prechádza hashmapu s prenosovými rýchlosťami služiebm v čase
            for (Map.Entry<Integer,Map> bitrate : bitrateMap.entrySet()) {
                Integer value = (Integer) bitrate.getValue().get(PID); // zistí bitrate pre aktuálny PID superiórneho cyklu
                value = (value==null) ? 0: value; //v prípade, že je nedefinovaný, priraď 0
                //pridá do série bod, ktorého x-osvú pozíciu predstavuje čas a y-ovú hodnotu aktuálna prenosová rýchlosť aktuálneho PIDu
                series.getData().add(new XYChart.Data(bitrate.getKey(), value));
            }
            chartData.add(series); //pridá do kolekcie kompletnú sériu
        }
        return chartData; //vracia kolekciu grafových dát
    }


    public void addListenersAndHandlers(Chart chart) {
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            chart.setPrefHeight(scene.getHeight());
        });

        groupByCheckBox.setOnAction(event -> {
            ((StackedAreaChart)chart).getData().clear();
            if(groupByCheckBox.isSelected()) {
                chart.setTitle(localization.getMultiplexProgramBitrateText());
                ((StackedAreaChart)chart).getData().addAll(createBitrateChart((Map)getLastItem(deltaServiceBitrateMap).getValue(),deltaServiceBitrateMap));
            }
            else {
                chart.setTitle(localization.getMultiplexBitrateText());
                ((StackedAreaChart)chart).getData().addAll(createBitrateChart((Map)getLastItem(deltaPIDBitrateMap).getValue(),deltaPIDBitrateMap));
            }
        });
    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
