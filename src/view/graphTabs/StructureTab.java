package view.graphTabs;


import static model.config.MPEG.*;
import app.streamAnalyzer.TimestampParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Stream;

import java.util.*;

import static app.Main.localization;
import static model.MapHandler.getLastItem;
import static model.MapHandler.updateMap;
import static model.config.Config.*;

public class StructureTab extends TimestampParser implements Graph{

    private Scene scene;
    public Tab tab;

    private CheckBox groupByCheckBox;
    private Stream stream;
    private Map<String,Map<Integer,Long>> bitrateMaps;
    private BarChart barChart;


    public StructureTab(){
        tab = new Tab(localization.getStructureTabText());
        groupByCheckBox = new CheckBox(localization.getGroupProgramsText());
        bitrateMaps = new LinkedHashMap<>();
    }


    public void drawGraph(Stream stream) {
        this.stream = stream;

        bitrateMaps.put("Min",stream.getTables().getMinBitrateMap());
        bitrateMaps.put("Avg",stream.getTables().getAvgPCRBitrateMap());
        bitrateMaps.put("Max",stream.getTables().getMaxBitrateMap());

        NumberAxis yAxis = new NumberAxis();
        CategoryAxis xAxis = new CategoryAxis();

        yAxis.setLabel(localization.getBitrateText());
        xAxis.setLabel("PID");

        yAxis.setTickLabelRotation(tickLabelRotation);
        xAxis.setTickLabelRotation(tickLabelRotation);

        yAxis.setTickLabelFormatter(
                new NumberAxis.DefaultFormatter(yAxis) {
                    @Override
                    public String toString(Number object) {
                        return String.format("%1$,.2f MBit/s", (Double)((object.doubleValue() * byteBinaryLength)/MegaBit));
                    }
                });

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setAnimated(false);
        barChart.setPadding(chartInsets);
        barChart.setPrefHeight(scene.getHeight());
        barChart.setLegendSide(Side.LEFT);

        addListenersAndHandlers(stream, barChart);

        groupByCheckBox.fire();

        HBox checkHBox = new HBox(groupByCheckBox);
        checkHBox.setAlignment(Pos.CENTER);
        checkHBox.setSpacing(chartHBoxSpacing);
        checkHBox.setPadding(chartHBoxInsets);

        tab.setContent(new VBox(barChart,checkHBox));
    }


    private Collection createStructureChartData(List<Integer> PIDlist, Map<String,Map<Integer, Long>> servicesMap ) {

        ObservableList<XYChart.Series> chartData = FXCollections.observableArrayList();
        XYChart.Series seriesMin = new XYChart.Series();
        XYChart.Series seriesAvg = new XYChart.Series();
        XYChart.Series seriesMax = new XYChart.Series();

        seriesMin.setName("Min");
        seriesAvg.setName("Avg");
        seriesMax.setName("Max");

        for (Integer PIDentry : PIDlist) {

            for(Map.Entry<String,Map<Integer, Long>> PIDbitrateMap : servicesMap.entrySet()) {

                for (Map.Entry<Integer, Long> entry : PIDbitrateMap.getValue().entrySet()) {
                    if (PIDentry.equals(entry.getKey())) {
                        switch (PIDbitrateMap.getKey()) {
                            case "Min":
                                seriesMin.getData().add(new XYChart.Data(getProgramName(stream,entry.getKey()), entry.getValue().intValue()));
                            case "Avg":
                                seriesAvg.getData().add(new XYChart.Data(getProgramName(stream,entry.getKey()), entry.getValue().intValue()));
                            case "Max":
                                seriesMax.getData().add(new XYChart.Data(getProgramName(stream,entry.getKey()), entry.getValue().intValue()));
                                // tooltip.setText(String.format("0x%04X", PIDentry.getKey() & 0xFFFFF) + " (" + PIDentry.getKey().toString() + ")", PIDentry.getValue());
                        }
                    }
                }
            }
        }
        chartData.addAll(seriesMin,seriesAvg,seriesMax);
        return chartData;
    }


    private Collection createServiceStructureChartData(List<Integer> PIDlist, Map<Integer,Integer> PMTmap, Map<String,Map<Integer, Long>> PIDbitrateMaps) {

        Map<String,Map<Integer,Long>> serviceBitrateMaps = new LinkedHashMap<>();

        for(Map.Entry<String,Map<Integer, Long>> PIDbitrateMap : PIDbitrateMaps.entrySet()) {
            Map<Integer,Long> serviceMap = new HashMap<>();

            for (Integer PIDentry : PIDlist) {
                Integer PID = PIDentry;
                Integer service = PMTmap.get(PID);

                for (Map.Entry<Integer, Long> minEntry : PIDbitrateMap.getValue().entrySet()) {
                    if (PID.equals(minEntry.getKey())) {
                        if (service != null) {
                            serviceMap = updateMap(serviceMap, service, minEntry.getValue());
                        }
                        else if (PID != null) {
                            serviceMap = updateMap(serviceMap, PID, minEntry.getValue());
                        }
                        // tooltip.setText(String.format("0x%04X", PIDentry.getKey() & 0xFFFFF) + " (" + PIDentry.getKey().toString() + ")", PIDentry.getValue());
                    }
                    serviceBitrateMaps.put(PIDbitrateMap.getKey(), serviceMap);
                }
            }
        }
        return createStructureChartData(createServicePIDlist(PIDlist, getLastItem(serviceBitrateMaps).getValue()), serviceBitrateMaps);
    }


    private List<Integer> createServicePIDlist(List<Integer> PIDlist, Map<Integer, Long> servicePIDmap) {
        if(servicePIDmap!=null) {
            List<Integer> servicePIDlist = new ArrayList<>();
            servicePIDlist.addAll(servicePIDmap.keySet());
            return servicePIDlist;
        }
        return PIDlist;
    }


    public void addListenersAndHandlers(Stream stream, Chart chart) {
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            this.barChart.setPrefHeight(scene.getHeight());
        });

        groupByCheckBox.setOnAction(event -> {
            this.barChart.getData().clear();
            if(groupByCheckBox.isSelected()) {
                this.barChart.setTitle(localization.getBitrateStructureByProgramsText());
                this.barChart.getData().addAll(createServiceStructureChartData(stream.getTables().getPIDlist(),stream.getTables().getPMTmap(),bitrateMaps));
            }
            else {
                chart.setTitle(localization.getBitrateStructureByPIDs());
                this.barChart.getData().addAll(createStructureChartData(stream.getTables().getPIDlist(), bitrateMaps));
            }
        });
    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }
}

