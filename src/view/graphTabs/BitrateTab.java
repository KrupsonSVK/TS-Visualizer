package view.graphTabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.*;

import model.Stream;
import app.streamAnalyzer.TimestampParser;

import static model.MapHandler.*;
import static model.config.MPEG.byteBinaryLength;
import static model.config.MPEG.tsPacketSize;

public class BitrateTab extends TimestampParser implements Graph{
    private Scene scene;
    public Tab  tab;

    public static final int tickUnit = 10;
    private CheckBox groupByCheckBox;
    private Map PIDmap;
    private Map deltaServiceBitrateMap;
    private Map deltaPIDBitrateMap;
    public BitrateTab(){
        tab = new Tab("Bitrate");
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


        double interval = 0.4;

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

        xAxis.setLabel("Time");
        yAxis.setLabel("Bitrate");

        StackedAreaChart stackedAreaChart = new StackedAreaChart<>(xAxis, yAxis);
        stackedAreaChart.setTitle("Multiplex Bitrate");
        stackedAreaChart.setCreateSymbols(false);
        stackedAreaChart.setPadding(new Insets(10,40,10,40));
        stackedAreaChart.setPrefHeight(scene.getHeight());
        stackedAreaChart.setAnimated(false);

        groupByCheckBox = new CheckBox("Group by programmes");
        HBox checkHBox = new HBox(groupByCheckBox);
        checkHBox.setAlignment(Pos.CENTER);
        checkHBox.setSpacing(10);
        checkHBox.setPadding(new Insets(10,10,10,10));

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


    private Collection createBitrateChart(Map<Integer,Integer> map, Map<Integer,Map> bitrateMap) {

        ObservableList<XYChart.Series> chartData = FXCollections.observableArrayList();

        for (Integer PID : map.keySet()) {
            final XYChart.Series series = new XYChart.Series<>();
            series.setName("PID: " + String.format("0x%04X", PID & 0xFFFF)  + " (" + PID + ")");

            for (Map.Entry<Integer,Map> bitrate : bitrateMap.entrySet()) {
                Integer value = (Integer) bitrate.getValue().get(PID);
                value = (value==null) ? 0: value;
                series.getData().add(new XYChart.Data(bitrate.getKey(), value));
            }
            chartData.add(series);
        }
        return chartData;
    }


    public void addListenersAndHandlers(Chart chart) {
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            chart.setPrefHeight(scene.getHeight());
        });

        groupByCheckBox.setOnAction(event -> {
            ((StackedAreaChart)chart).getData().clear();
            if(groupByCheckBox.isSelected()) {
                chart.setTitle("Multiplex program bitrate");
                ((StackedAreaChart)chart).getData().addAll(createBitrateChart((Map)getLastItem(deltaServiceBitrateMap).getValue(),deltaServiceBitrateMap));
            }
            else {
                chart.setTitle("Multiplex bitrate");
                ((StackedAreaChart)chart).getData().addAll(createBitrateChart((Map)getLastItem(deltaPIDBitrateMap).getValue(),deltaPIDBitrateMap));
            }
        });
    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
