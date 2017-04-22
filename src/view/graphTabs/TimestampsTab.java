package view.graphTabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.Map;

import model.Stream;
import model.Timestamp;
import view.Window;
import view.visualizationTab.VisualizationTab;

import static model.Sorter.sortHashMapByKey;


public class TimestampsTab extends VisualizationTab implements Graph{

    private Scene scene;
    public Tab tab;
    private Stream stream;

    private Label captionLabel;
    private ScatterChart scatterChart;
    private ComboBox<String> filterComboBox;

    private EventHandler<ActionEvent> filterComboBoxEvent;

    private Map filteredPIDs;

    public static final int tickUnit = 10;


    public TimestampsTab(){
        tab = new Tab("Timestamps");
        captionLabel = new Label("");
    }


    public void drawGraph(Stream streamDescriptor) {

        this.stream = streamDescriptor;

        Map sortedBitrateMap = sortHashMapByKey(stream.getTables().getBitrateMap());
        Map deltaBitrateMap = createDeltaBitrateMap(sortedBitrateMap);

        filterComboBox = createFilterComboBox(stream);
        scatterChart = createScatterChart(sortedBitrateMap);

        HBox filterHBox = new HBox(new Label("Program filter: "), filterComboBox);
        filterHBox.setAlignment(Pos.CENTER);
        filterHBox.setSpacing(10);
        filterHBox.setPadding(new Insets(10,10,10,10));

        captionLabel.setTextFill(Color.DARKORANGE);
        captionLabel.setStyle("-fx-font: 24 arial;");
        captionLabel.toFront();

        addListenersAndHandlers(scatterChart);
        filterComboBox.setOnAction(filterComboBoxEvent);

        tab.setContent(new VBox(captionLabel,scatterChart,filterHBox));
    }


    private<K,V>ScatterChart createScatterChart(Map<K,V> map) {

        final NumberAxis xAxis = new NumberAxis(0, 10, 1);
        final NumberAxis yAxis = new NumberAxis(-100, 500, 100);
        final ScatterChart scatterChart = new ScatterChart<>(xAxis, yAxis);

        xAxis.setLabel("Age (years)");
        yAxis.setLabel("Returns to date");

        scatterChart.setTitle("Timestamp layout");

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Equities");
        series1.getData().add(new XYChart.Data(4.2, 193.2));
        series1.getData().add(new XYChart.Data(2.8, 33.6));
        series1.getData().add(new XYChart.Data(6.2, 24.8));
        series1.getData().add(new XYChart.Data(1, 14));
        series1.getData().add(new XYChart.Data(1.2, 26.4));
        series1.getData().add(new XYChart.Data(4.4, 114.4));
        series1.getData().add(new XYChart.Data(8.5, 323));
        series1.getData().add(new XYChart.Data(6.9, 289.8));
        series1.getData().add(new XYChart.Data(9.9, 287.1));
        series1.getData().add(new XYChart.Data(0.9, -9));
        series1.getData().add(new XYChart.Data(3.2, 150.8));
        series1.getData().add(new XYChart.Data(4.8, 20.8));
        series1.getData().add(new XYChart.Data(7.3, -42.3));
        series1.getData().add(new XYChart.Data(1.8, 81.4));
        series1.getData().add(new XYChart.Data(7.3, 110.3));
        series1.getData().add(new XYChart.Data(2.7, 41.2));

        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Mutual funds");
        series2.getData().add(new XYChart.Data(5.2, 229.2));
        series2.getData().add(new XYChart.Data(2.4, 37.6));
        series2.getData().add(new XYChart.Data(3.2, 49.8));
        series2.getData().add(new XYChart.Data(1.8, 134));
        series2.getData().add(new XYChart.Data(3.2, 236.2));
        series2.getData().add(new XYChart.Data(7.4, 114.1));
        series2.getData().add(new XYChart.Data(3.5, 323));
        series2.getData().add(new XYChart.Data(9.3, 29.9));
        series2.getData().add(new XYChart.Data(8.1, 287.4));

        XYChart.Series series3 = new XYChart.Series();
        series3.setName("PIDs");

        ObservableList<ScatterChart.Data> scatterChartData = FXCollections.observableArrayList();

        for(Map.Entry<K,V> PIDentry : map.entrySet()){
//            scatterChartData.add(new ScatterChart.Data(PIDentry.getKey()  , PIDentry.getValue()));
        }
        series3.getData().addAll(scatterChartData);

        scatterChart.getData().addAll(series1, series2, series3);

        scatterChart.setLegendSide(Side.LEFT);
        scatterChart.toBack();
        scatterChart.setPadding(new Insets(0,40,0,40));
        scatterChart.setPrefHeight(scene.getHeight());

        return scatterChart;
    }


    public void addListenersAndHandlers(Chart chart) {
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            chart.setPrefHeight(scene.getHeight());
        });

        filterComboBoxEvent = (ActionEvent event) -> {
            filteredPIDs = filterProgram(filterComboBox.getValue(),stream.getTables().getPMTmap(),stream.getTables().getProgramMap());
            groupProgrammes(filteredPIDs,stream.getTables().getPMTmap(),stream.getTables().getProgramMap(),stream.getTables().getPATmap());
        };

//        for (final Object data : ((ScatterChart)chart).getData()) {
//            ((ScatterChart.Series)data).getNode().setOnMouseMoved( event -> {
//                captionLabel.setTranslateX(event.getSceneX());
//                captionLabel.setTranslateY(event.getSceneY());
//                captionLabel.setText(String.valueOf( ((ScatterChart.Data)data).getExtraValue()) + "PCR: 00:00:03,435");
//            });
//        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}

