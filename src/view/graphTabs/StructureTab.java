package view.graphTabs;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Stream;
import app.streamAnalyzer.TimestampParser;

import java.util.*;

import static model.MapHandler.updateMap;
import static model.config.MPEG.byteBinaryLength;

public class StructureTab extends TimestampParser implements Graph{

    private Scene scene;
    public Tab tab;

    public static final int tickUnit = 10;
    private CheckBox groupByCheckBox;
    private Stream stream;
    private Map<String,Map<Integer,Long>> bitrateMaps;
    private BarChart barChart;


    public StructureTab(){
        tab = new Tab("Structure");
        groupByCheckBox = new CheckBox("Group by programmes");
        bitrateMaps = new LinkedHashMap<>();
    }


    public void drawGraph(Stream stream) {
        this.stream = stream;

        bitrateMaps.put("Min",stream.getTables().getMinBitrateMap());
        bitrateMaps.put("Avg",stream.getTables().getAvgPCRBitrateMap());
        bitrateMaps.put("Max",stream.getTables().getMaxBitrateMap());

        NumberAxis yAxis = new NumberAxis();
        CategoryAxis xAxis = new CategoryAxis();

        yAxis.setLabel("Bitrate");
        xAxis.setLabel("PID");
//        yAxis.setTickUnit(10000000);
//        yAxis.setUpperBound(100000000);
//        yAxis.setLowerBound(0);
        yAxis.setTickLabelRotation(0);
        xAxis.setTickLabelRotation(0);

        yAxis.setTickLabelFormatter(
                new NumberAxis.DefaultFormatter(yAxis) {
                    @Override
                    public String toString(Number object) {
                        return String.format("%1$,.2f MBit/s", (Double)(object.doubleValue()*byteBinaryLength/(1024.*1024.)));
                    }
                });

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setAnimated(false);
        barChart.setPadding(new Insets(10,40,10,40));
        barChart.setPrefHeight(scene.getHeight());
        barChart.setLegendSide(Side.LEFT);

        addListenersAndHandlers(barChart);

        groupByCheckBox.fire();

        HBox checkHBox = new HBox(groupByCheckBox);
        checkHBox.setAlignment(Pos.CENTER);
        checkHBox.setSpacing(10);
        checkHBox.setPadding(new Insets(10,10,10,10));

        tab.setContent(new VBox(barChart,checkHBox));
    }


    private Collection createStructureChartData(Map<Integer,Integer> PIDmap, Map<String,Map<Integer, Long>> servicesMap ) {

        ObservableList<XYChart.Series> chartData = FXCollections.observableArrayList();
        XYChart.Series seriesMin = new XYChart.Series();
        XYChart.Series seriesAvg = new XYChart.Series();
        XYChart.Series seriesMax = new XYChart.Series();

        seriesMin.setName("Min");
        seriesAvg.setName("Avg");
        seriesMax.setName("Max");

        for (Map.Entry<Integer, Integer> PIDentry : PIDmap.entrySet()) {

            for(Map.Entry<String,Map<Integer, Long>> PIDbitrateMap : servicesMap.entrySet()) {

                for (Map.Entry<Integer, Long> entry : PIDbitrateMap.getValue().entrySet()) {
                    if (PIDentry.getKey().equals(entry.getKey())) {
                        switch (PIDbitrateMap.getKey()) {
                            case "Min":
                                seriesMin.getData().add(new XYChart.Data(entry.getKey().toString(), entry.getValue().intValue()));
                            case "Avg":
                                seriesAvg.getData().add(new XYChart.Data(entry.getKey().toString(), entry.getValue().intValue()));
                            case "Max":
                                seriesMax.getData().add(new XYChart.Data(entry.getKey().toString(), entry.getValue().intValue()));
                                // tooltip.setText(String.format("0x%04X", PIDentry.getKey() & 0xFFFFF) + " (" + PIDentry.getKey().toString() + ")", PIDentry.getValue());
                        }
                    }
                }
            }
        }
        chartData.addAll(seriesMin,seriesAvg,seriesMax);
        return chartData;
    }





    private Collection createServiceStructureChartData(Map<Integer,Integer> PIDmap, Map<Integer,Integer> PMTmap, Map<String,Map<Integer, Long>> PIDbitrateMaps) {

        Map<String,Map<Integer,Long>> serviceBitrateMaps = new LinkedHashMap<>();

        for(Map.Entry<String,Map<Integer, Long>> PIDbitrateMap : PIDbitrateMaps.entrySet()) {
            Map<Integer,Long> serviceMap = new HashMap<>();

            for (Map.Entry<Integer, Integer> PIDentry : PIDmap.entrySet()) {
                Integer PID = PIDentry.getKey();
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
        return createStructureChartData(PIDmap,serviceBitrateMaps);
    }

    public void addListenersAndHandlers(Chart chart) {
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            this.barChart.setPrefHeight(scene.getHeight());
        });

        groupByCheckBox.setOnAction(event -> {
            this.barChart.getData().clear();
            if(groupByCheckBox.isSelected()) {
                this.barChart.setTitle("Bitrate structure by programmes");
                this.barChart.getData().addAll(createServiceStructureChartData(stream.getTables().getPIDmap(),stream.getTables().getPMTmap(),bitrateMaps));
            }
            else {
                chart.setTitle("Bitrate structure by PID");
                this.barChart.getData().addAll(createStructureChartData(stream.getTables().getPIDmap(), bitrateMaps));
            }
        });
    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }
}

