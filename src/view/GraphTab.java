package view;

import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.Map;

import model.Stream;

public class GraphTab {
    Scene scene;
    public Tab  tab;

    GraphTab(Scene scene){
        this.scene = scene;
        tab = new Tab("Graph");
    }

    public void drawGraph(Stream stream) {

        final NumberAxis xAxis = new NumberAxis(1, 12, 1);
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Time");
        yAxis.setLabel("Bitrate");

        final StackedAreaChart<Number, Number> stackedAreaChart = new StackedAreaChart<>(xAxis, yAxis);
        stackedAreaChart.setTitle("Multiplex Bitrate Chart");

        HashMap<Integer, Integer> PIDmap = stream.getPIDs();

        for (Map.Entry<Integer, Integer> pid : PIDmap.entrySet()) {
            final XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("PID: " + ("0x" + Integer.toHexString(pid.getKey()) + " (" + pid.getKey() + ")"));

            for (int i = 1; i <= 12; i += 1)
                series.getData().add(new XYChart.Data(i, (Math.random() * (pid.getKey()) + 1)));

            stackedAreaChart.getData().addAll(series);
        }

        ScrollPane scrollPane = new ScrollPane(stackedAreaChart);
        scrollPane.setPrefHeight(scene.getHeight() * 0.6);//60%
        scrollPane.setFitToWidth(true);

        tab.setContent(new VBox(stackedAreaChart));
    }
}
