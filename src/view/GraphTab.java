package view;

import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Stream;

public class GraphTab extends Window{
    private Scene scene;
    Tab  tab;

    GraphTab(){
        tab = new Tab("Graph");
    }

    public void drawGraph(Stream stream) {

        List timeList = stream.getTables().getTimeListSorted();
        long startTimeStamp = 0; // (long) timeList.get(0);
        long endTimeStamp = 0; // (long) timeList.get(timeList.size() - 1);

        final Axis xAxis = new NumberAxis("Time", parseTime(startTimeStamp), parseTime(endTimeStamp),timeList.size());

        final NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Time");
        yAxis.setLabel("Bitrate");

        final StackedAreaChart<Number, Number> stackedAreaChart = new StackedAreaChart<>(xAxis, yAxis);
        stackedAreaChart.setTitle("Multiplex Bitrate Chart");

        HashMap<Integer, Integer> PIDmap = stream.getTables().getPacketsSizeMap();

        for (Map.Entry<Integer, Integer> pid : PIDmap.entrySet()) {
            final XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("PID: " + ("0x" + Integer.toHexString(pid.getKey()) + " (" + pid.getKey() + ")"));

            for (int i = 1; i <= 12; i += 1) {
                series.getData().add(new XYChart.Data(i, pid.getKey()));
            }
            stackedAreaChart.getData().addAll(series);
        }

        ScrollPane scrollPane = new ScrollPane(stackedAreaChart);
        scrollPane.setPrefHeight(scene.getHeight() * 0.6);//60%
        scrollPane.setFitToWidth(true);

        tab.setContent(new VBox(stackedAreaChart));
    }


    private long midBits(long k, int m, int n){
        return (k >> m) & ((1 << (n-m))-1);
    }


    private double parseTime(long timeStamp) {
        return  (midBits(timeStamp,17,35) << 15) | midBits(timeStamp,1,16);
    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
