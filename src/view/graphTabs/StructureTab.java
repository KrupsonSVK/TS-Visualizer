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
import model.config.MPEG;

import java.util.*;

import static model.config.MPEG.TimestampType.*;

public class StructureTab extends TimestampParser implements Graph{

    private Scene scene;
    public Tab tab;

    public static final int tickUnit = 10;
    private CheckBox groupByCheckBox;
    private Stream stream;
    private Map<String,Map<Integer,Long>> bitrateMaps;


    public StructureTab(){
        tab = new Tab("Structure");
        groupByCheckBox = new CheckBox("Group by programmes");
        bitrateMaps = new LinkedHashMap<>();
    }


    public void drawGraph(Stream stream) {
        this.stream = stream;

        bitrateMaps.put("Min",stream.getTables().getMinBitrateMap());
        bitrateMaps.put("Avg",stream.getTables().getAvgBitrateMap());
        bitrateMaps.put("Max",stream.getTables().getMaxBitrateMap());

        NumberAxis xAxis = new NumberAxis();
        CategoryAxis  yAxis = new CategoryAxis();
        BarChart barChart = new BarChart<>(xAxis, yAxis);

        xAxis.setLabel("Bitrate");
        yAxis.setLabel("PID");

        xAxis.setTickLabelRotation(0);
        yAxis.setTickLabelRotation(0);

        xAxis.setTickLabelFormatter(
                new NumberAxis.DefaultFormatter(xAxis) {
                    @Override
                    public String toString(Number object) {
                        return String.format("%1$,.2f MBit/s", (Double)(object.doubleValue()));
                    }
                });

        barChart.setAnimated(true);
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
                                seriesMin.getData().add(new XYChart.Data(entry.getValue().intValue(), entry.getKey().toString()));
                            case "Avg":
                                seriesAvg.getData().add(new XYChart.Data(entry.getValue().intValue(), entry.getKey().toString()));
                            case "Max":
                                seriesMax.getData().add(new XYChart.Data(entry.getValue().intValue(), entry.getKey().toString()));
                                // tooltip.setText(String.format("0x%04X", PIDentry.getKey() & 0xFFFFF) + " (" + PIDentry.getKey().toString() + ")", PIDentry.getValue());
                        }
                    }
                }
            }
        }
        chartData.addAll(seriesMin,seriesAvg,seriesMax);
        return chartData;
    }


    private Map updateMap(Map<Integer, Long> map, Integer key, Long value) {
        Long currentValue = map.get(key);
        if(currentValue != null){
            value += currentValue;
        }
        map.put(key,value);
        return map;
    }


    private Collection createServiceStructureChartData(Map<Integer,Integer> PIDmap, Map<Integer,Integer> PMTmap, Map<String,Map<Integer, Long>> PIDbitrateMaps) {
//, Map<Integer,Integer> PMTmap, Map<Integer,Long> minPIDMap, Map<Integer,Long> avgPIDMap, Map<Integer,Long> maxPIDMap
        Map<String,Map<Integer,Long>> serviceBitrateMaps = new LinkedHashMap<>();

        for(Map.Entry<String,Map<Integer, Long>> PIDbitrateMap : PIDbitrateMaps.entrySet()) {
            Map<Integer,Long> serviceMap = new HashMap<>();

            for (Map.Entry<Integer, Integer> PIDentry : PIDmap.entrySet()) {
                Integer PID = PIDentry.getKey();
                Integer service = PMTmap.get(PID);

                for (Map.Entry<Integer, Long> minEntry : PIDbitrateMap.getValue().entrySet()) {
                    if (PID.equals(minEntry.getKey())) {
                        if (service == null) {
                            serviceMap = updateMap(serviceMap, PID, minEntry.getValue());
                        }
                        serviceMap = updateMap(serviceMap, service, minEntry.getValue());
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
            chart.setPrefHeight(scene.getHeight());
        });

        groupByCheckBox.setOnAction(event -> {
            ((BarChart)chart).getData().clear();
            if(groupByCheckBox.isSelected()) {
                chart.setTitle("Bitrate structure by programmes");
                ((BarChart)chart).getData().addAll(createServiceStructureChartData(stream.getTables().getPIDmap(),stream.getTables().getPMTmap(),bitrateMaps));
            }
            else {
                chart.setTitle("Bitrate structure by PID");
                ((BarChart)chart).getData().addAll(createStructureChartData(stream.getTables().getPIDmap(), bitrateMaps));
            }
        });
    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }
}

