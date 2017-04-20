package view.graphTabs;

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
import model.Timestamp;
import static model.Sorter.*;

public class BitrateTab extends Timestamp implements Graph{
    private Scene scene;
    public Tab  tab;

    public static final int tickUnit = 10;
    private CheckBox groupByCheckBox;

    public BitrateTab(){
        tab = new Tab("Bitrate");
    }


    public void drawGraph(Stream stream) {

        Map sortedBitrateMap = sortHashMapByKey(stream.getTables().getBitrateMap());
        Map deltaBitrateMap = createDeltaBitrateMap(sortedBitrateMap);

       long startTimeStamp = (long) getFirstItem( stream.getTables().getPCRmap()).getKey();
       long endTimeStamp = (long) getLastItem( stream.getTables().getPCRmap()).getKey();
       long intervalTime = endTimeStamp - startTimeStamp;
       long tickInterval = intervalTime / deltaBitrateMap.size();


        long ticks = deltaBitrateMap.size();
        double interval = 0.4;

        final NumberAxis xAxis = new NumberAxis(0,deltaBitrateMap.size()-1,tickUnit);
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setTickLabelFormatter(
                new NumberAxis.DefaultFormatter(yAxis) {
                    @Override
                    public String toString(Number object) {
                        long timestamp = startTimeStamp + (tickInterval * object.intValue());
                        String out = parseTimestamp(timestamp);
                        return String.format("%s",out);
                    }
                });
        yAxis.setTickLabelFormatter(
                new NumberAxis.DefaultFormatter(yAxis) {
                    @Override
                    public String toString(Number object) {
                        return String.format("%1$,.2f MBit/s", (Double)(object.doubleValue() * interval));
                    }
                });

        xAxis.setLabel("Time");
        yAxis.setLabel("Bitrate");

        StackedAreaChart stackedAreaChart = new StackedAreaChart<>(xAxis, yAxis);
        stackedAreaChart.setTitle("Multiplex Bitrate");
        stackedAreaChart.setCreateSymbols(false);
        stackedAreaChart.setPadding(new Insets(10,40,10,40));
        stackedAreaChart.setPrefHeight(scene.getHeight());

        Map PIDmap = sortHashMapByKey(stream.getTables().getPIDmap());
        stackedAreaChart.getData().addAll(createBitrateChart(PIDmap,deltaBitrateMap));

        groupByCheckBox = new CheckBox("Group by programmes");
        HBox checkHBox = new HBox(groupByCheckBox);
        checkHBox.setAlignment(Pos.CENTER);
        checkHBox.setSpacing(10);
        checkHBox.setPadding(new Insets(10,10,10,10));

        addListenersAndHandlers(stackedAreaChart);

        tab.setContent(new VBox(stackedAreaChart,checkHBox));
    }

    private Collection createBitrateChart(Map map, Map bitrateMap) {

        Collection chartData = null;// = new Collection(); // = new ArrayList<XYChart.Series>();
        for (Map.Entry<Integer,Integer> pid : ((HashMap<Integer,Integer>)map).entrySet()) {

            Integer PID = pid.getKey();
            final XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("PID: " + String.format("0x%04X", PID & 0xFFFF)  + " (" + PID + ")");

            for (Map.Entry<Integer,Map> bitrate : ((HashMap<Integer,Map>)bitrateMap).entrySet()) {
                Integer value = (Integer) bitrate.getValue().get(PID);
                value = (value==null) ? 0: value;
                series.getData().add(new XYChart.Data(bitrate.getKey(), value));

            }
            //chartData.add(series);
        }

        return chartData;
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
