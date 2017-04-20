package view.graphTabs;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Stream;
import model.Timestamp;

import java.util.Collection;
import java.util.Map;

import static model.Sorter.sortHashMapByKey;

public class StructureTab extends Timestamp implements Graph{

    private Scene scene;
    public Tab tab;

    public static final int tickUnit = 10;
    private CheckBox groupByCheckBox;


    public StructureTab(){
        tab = new Tab("Structure");
    }


    public void drawGraph(Stream stream) {

        Map sortedBitrateMap = sortHashMapByKey(stream.getTables().getBitrateMap());
        Map deltaBitrateMap = createDeltaBitrateMap(sortedBitrateMap);

        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        final BarChart barChart = new BarChart<>(xAxis, yAxis);

        xAxis.setTickLabelRotation(90);

        barChart.setTitle("Bitrate structure");
        xAxis.setLabel("Bitrate");
        yAxis.setLabel("PID");

        barChart.setPadding(new Insets(10,40,10,40));
        barChart.setPrefHeight(scene.getHeight());

        barChart.getData().addAll(createStructureChart());

        addListenersAndHandlers(barChart);

        groupByCheckBox = new CheckBox("Group by programmes");
        HBox checkHBox = new HBox(groupByCheckBox);
        checkHBox.setAlignment(Pos.CENTER);
        checkHBox.setSpacing(10);
        checkHBox.setPadding(new Insets(10,10,10,10));

        tab.setContent(new VBox(barChart));
    }


    private Collection createStructureChart() {

        final String austria = "Austria";
        final String brazil = "Brazil";
        final String france = "France";
        final String italy = "Italy";
        final String usa = "USA";

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("2003");
        series1.getData().add(new XYChart.Data(25601.34, austria));
        series1.getData().add(new XYChart.Data(20148.82, brazil));
        series1.getData().add(new XYChart.Data(10000, france));
        series1.getData().add(new XYChart.Data(35407.15, italy));
        series1.getData().add(new XYChart.Data(12000, usa));

        XYChart.Series series2 = new XYChart.Series();
        series2.setName("2004");
        series2.getData().add(new XYChart.Data(57401.85, austria));
        series2.getData().add(new XYChart.Data(41941.19, brazil));
        series2.getData().add(new XYChart.Data(45263.37, france));
        series2.getData().add(new XYChart.Data(117320.16, italy));
        series2.getData().add(new XYChart.Data(14845.27, usa));

        XYChart.Series series3 = new XYChart.Series();
        series3.setName("2005");
        series3.getData().add(new XYChart.Data(45000.65, austria));
        series3.getData().add(new XYChart.Data(44835.76, brazil));
        series3.getData().add(new XYChart.Data(18722.18, france));
        series3.getData().add(new XYChart.Data(17557.31, italy));
        series3.getData().add(new XYChart.Data(92633.68, usa));
        return null; //TODO collections
    }


    public void addListenersAndHandlers(Chart chart) {
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            chart.setPrefHeight(scene.getHeight());
        });

        groupByCheckBox.setOnAction(event -> {
            System.out.println("Group by programmes");
        });
    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }
}

