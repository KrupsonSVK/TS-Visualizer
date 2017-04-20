package view.graphTabs;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.Map;

import model.Stream;
import model.Timestamp;

import static model.Sorter.sortHashMapByKey;


public class CompositionTab extends Timestamp implements Graph{

    private Scene scene;
    public Tab tab;
    private Stream stream;

    private Label captionLabel;
    private RadioButton PIDradioButton;
    private RadioButton programRadioButton;
    private RadioButton streamRadioButton;
    private PieChart pieChart;
    private HBox radioButtonHBox;


    public static final int tickUnit = 10;


    public CompositionTab(){
        tab = new Tab("Composition");
        pieChart = new PieChart();
        PIDradioButton = new RadioButton("PID composition");
        programRadioButton = new RadioButton("Program composition");
        streamRadioButton = new RadioButton("Stream type composition");
        captionLabel = new Label("");
    }


    public void drawGraph(Stream streamDescriptor) {

        this.stream = streamDescriptor;
        Map sortedBitrateMap = sortHashMapByKey(stream.getTables().getBitrateMap());
        Map deltaBitrateMap = createDeltaBitrateMap(sortedBitrateMap);

        radioButtonHBox = new HBox(PIDradioButton,programRadioButton,streamRadioButton);
        radioButtonHBox.setAlignment(Pos.CENTER);
        radioButtonHBox.setSpacing(10);
        radioButtonHBox.setPadding(new Insets(10,10,10,10));

        captionLabel.setTextFill(Color.DARKORANGE);
        captionLabel.setStyle("-fx-font: 24 arial;");
        captionLabel.toFront();

        addListenersAndHandlers(pieChart);

        tab.setContent(new VBox(captionLabel,pieChart,radioButtonHBox));

        PIDradioButton.fire();
    }


    private<K,V>PieChart createPieChart(Map<K,V> map) {

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for(Map.Entry<K,V> PIDentry : map.entrySet()){
            pieChartData.add(new PieChart.Data(String.format("0x%04X", (Integer)PIDentry.getKey() & 0xFFFFF)  + " (" + PIDentry.getKey().toString()  + ")", (Integer)PIDentry.getValue()));
        }

        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setLabelLineLength(10);
        pieChart.setLegendSide(Side.LEFT);
        pieChart.toBack();
        pieChart.setPadding(new Insets(10,40,10,40));
        pieChart.setPrefHeight(scene.getHeight());

        return pieChart;
    }


    public void addListenersAndHandlers(Chart chart) {
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            chart.setPrefHeight(scene.getHeight());
        });

        PIDradioButton.setOnAction( event -> {
            programRadioButton.setSelected(false);
            streamRadioButton.setSelected(false);

            Map PIDmap = stream.getTables().getPIDmap();
            pieChart = createPieChart(PIDmap);
            pieChart.setTitle("PID composition");
            addListenersAndHandlers(pieChart);
            tab.setContent(new VBox(captionLabel,pieChart,radioButtonHBox));
        });

        programRadioButton.setOnAction( event -> {
            PIDradioButton.setSelected(false);
            streamRadioButton.setSelected(false);

            Map programMap = stream.getTables().getPIDmap();
            pieChart = createPieChart(programMap);
            pieChart.setTitle("Program compostion");
            addListenersAndHandlers(pieChart);
            tab.setContent(new VBox(captionLabel,pieChart,radioButtonHBox));
        });

        streamRadioButton.setOnAction( event -> {
            PIDradioButton.setSelected(false);
            programRadioButton.setSelected(false);

            Map streamMap = stream.getTables().getPIDmap();
            pieChart = createPieChart(streamMap);
            pieChart.setTitle("Stream types composition");
            addListenersAndHandlers(pieChart);
            tab.setContent(new VBox(captionLabel,pieChart,radioButtonHBox));
        });

        for (final PieChart.Data data : ((PieChart)chart).getData()) {
            data.getNode().setOnMouseMoved( event -> {
                captionLabel.setTranslateX(event.getSceneX());
                captionLabel.setTranslateY(event.getSceneY());
                captionLabel.setText(String.valueOf(data.getPieValue()) + "% of stream");
            });
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}

