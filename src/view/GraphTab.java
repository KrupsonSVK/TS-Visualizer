package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

import java.math.BigInteger;
import java.util.*;

import model.Sorter;
import model.Stream;

import static model.Sorter.*;

public class GraphTab extends Window{
    private Scene scene;
    Tab  tab;

    GraphTab(){
        tab = new Tab("Graph");
    }

    public void drawGraph(Stream stream) {

        Map sortedBitrateMap = sortHashMapByKey(stream.getTables().getBitrateMap());
        Map deltaBitrateMap = createDeltaBitrateMap(sortedBitrateMap);

       long startTimeStamp = (long) getFirstItem(PCRbitrateMap).getKey();
       long endTimeStamp = (long) getLastItem(PCRbitrateMap).getKey();
       long intervalTime = endTimeStamp - startTimeStamp;
       long tickInterval = intervalTime / deltaBitrateMap.size();


        long ticks = deltaBitrateMap.size();
        double interval = 0.4;

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setTickLabelFormatter(
                new NumberAxis.DefaultFormatter(yAxis) {
                    @Override public String toString(Number object) {
                        String out =  new String(String.format("%1$,.2f MBit/s", (Double)(object.doubleValue() * interval)));
                        System.out.println(out);
                        return String.format("%s",out);
                    }
                });

        yAxis.setTickLabelFormatter(
                new NumberAxis.DefaultFormatter(yAxis) {
                    @Override public String toString(Number object) {
                        String out =  new String(String.format("%1$,.2f MBit/s", (Double)(object.doubleValue() * interval)));
                        System.out.println(out);
                        return String.format("%s",out);
                    }
                });

        xAxis.setLabel("Time");
        yAxis.setLabel("Bitrate");

        StackedAreaChart stackedAreaChart = new StackedAreaChart<>(xAxis, yAxis);
        stackedAreaChart.setTitle("Multiplex Bitrate Chart");
        stackedAreaChart.setCreateSymbols(false);
        stackedAreaChart.setPadding(new Insets(10,40,10,40));
        stackedAreaChart.setPrefHeight(scene.getHeight());

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            stackedAreaChart.setPrefHeight(scene.getHeight());
        });

        Map PIDmap = sortHashMapByKey(stream.getTables().getPIDmap());

        for (Map.Entry<Integer,Integer> pid : ((HashMap<Integer,Integer>)PIDmap).entrySet()) {

            Integer PID = pid.getKey();
            final XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("PID: " + ("0x" + Integer.toHexString(PID) + " (" + PID + ")")); //TODO stringformat

            for (Map.Entry<Integer,Map> bitrate : ((HashMap<Integer,Map>)deltaBitrateMap).entrySet()) {
                Integer value = (Integer) bitrate.getValue().get(PID);
                value = (value==null) ? 0: value;

                series.getData().add(new XYChart.Data(bitrate.getKey(), value));
            }
            stackedAreaChart.getData().add(series);
        }

        ScrollPane scrollPane = new ScrollPane(stackedAreaChart);
        //scrollPane.setPrefHeight(scene.getHeight() * 0.6);//60%
//        stackedAreaChart.setMaxWidth(scene.getWidth()-50);
        scrollPane.setFitToWidth(true);
//        scrollPane.setPadding(new Insets(20,20,20,20));
        tab.setContent(new VBox(stackedAreaChart));
    }


    private <K,V extends Map<K, V>> Map createDeltaBitrateMap(Map<K,V> bitrateMap) {

        NavigableMap<K, V> navigablePCRmap = new TreeMap<>(bitrateMap);
        Map bitratePCRmap = new HashMap<K,V>();

        for (Map.Entry<K, V> currentPIDmap : navigablePCRmap.entrySet()) {
            Map.Entry<K, V> previousPIDmap = navigablePCRmap.lowerEntry(currentPIDmap.getKey());
            V previousPIDmapValue = (previousPIDmap==null) ? null : previousPIDmap.getValue();
            bitratePCRmap.put(currentPIDmap.getKey(),calculateBitrateDelta(currentPIDmap.getValue(),previousPIDmapValue));
        }

        return sortHashMapByKey(bitratePCRmap);
    }


    private<K,V> Map calculateBitrateDelta(Map<K,V> current, Map<K,V> previous) {
        if (previous==null) {
            return current;
        }
        Map deltaMap = new HashMap<K, V>();
        for (Map.Entry<K, V> currentEntry : current.entrySet()) {
            K currentKey = currentEntry.getKey();
            Integer previousValue = (Integer) previous.get(currentKey);
            previousValue = (previousValue==null) ? 0 : previousValue;
            Integer currentValue = (currentEntry.getValue()==null) ? 0 : (Integer)currentEntry.getValue();

            deltaMap.put(currentKey,currentValue - previousValue);
        }
        return deltaMap;
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
