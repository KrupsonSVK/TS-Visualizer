package view.graphTabs;


import com.sun.javafx.charts.Legend;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

import model.Stream;
import app.streamAnalyzer.TimestampParser;

import static model.config.MPEG.getElementaryStreamDescriptor;
import static model.config.MPEG.tsPacketSize;


public class CompositionTab extends TimestampParser implements Graph{

    private Scene scene;
    public Tab tab;
    private Stream stream;

    private Tooltip tooltip;
    private RadioButton PIDradioButton;
    private RadioButton programRadioButton;
    private RadioButton streamRadioButton;
    private PieChart pieChart;
    private HBox radioButtonHBox;

    private Map tooltips;


    public static final int tickUnit = 10;
    private VBox vbox;


    public CompositionTab(){
        tab = new Tab("Composition");
        pieChart = new PieChart();
        PIDradioButton = new RadioButton("PID composition");
        programRadioButton = new RadioButton("Program composition");
        streamRadioButton = new RadioButton("Stream type composition");
        tooltip = new Tooltip("");
        tooltips = new HashMap();
    }


    public void drawGraph(Stream streamDescriptor) {

        this.stream = streamDescriptor;
//        Map sortedBitrateMap = sortHashMapByKey(stream.getTables().getIndexSnapshotMap());
//        Map deltaBitrateMap = createDeltaBitrateMap(sortedBitrateMap);

        radioButtonHBox = new HBox(PIDradioButton,programRadioButton,streamRadioButton);
        radioButtonHBox.setAlignment(Pos.CENTER);
        radioButtonHBox.setSpacing(10);
        radioButtonHBox.setPadding(new Insets(0,10,10,10));

        tooltip.hide();
        //tooltipA.hide();

        addListenersAndHandlers(pieChart);

        PIDradioButton.fire();
    }


    private PieChart createPieChart(Map<String, Integer> map, int totalSize, Double heigth) {

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
//        Iterator it = tooltips.entrySet().iterator();
//        while(it.hasNext()){
//            ((Tooltip)it.next()).hide();
//        }
        tooltips.clear();

        for(Map.Entry<String,Integer> PIDentry : map.entrySet()){
            PieChart.Data data = new PieChart.Data(PIDentry.getKey(), PIDentry.getValue());
            pieChartData.add(data);

            float size = PIDentry.getValue()*tsPacketSize/1024f/1024f;
            int percentage = (int) (((PIDentry.getValue()).doubleValue() / totalSize)*100f);
            tooltips.put( data.hashCode() , new Tooltip(String.format("%.2f MB (%d", size, percentage) + "%)") );
        }

        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setLabelLineLength(10);
        pieChart.setLegendSide(Side.BOTTOM);
        pieChart.toBack();
        pieChart.setPadding(new Insets(10,40,10,40));
        pieChart.setPrefHeight(scene.getHeight());
        pieChart.setAnimated(true);

        if(heigth != null) {
            ((Legend) pieChart.lookup(".chart-legend")).setPrefHeight(heigth.doubleValue());
        }
        pieChart.setLabelsVisible(true);
        return pieChart;
    }


    public void addListenersAndHandlers(Chart chart) {
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            pieChart.setPrefHeight(scene.getHeight());
            addListenersAndHandlers(pieChart);
        });

        PIDradioButton.setOnAction( event -> {
            programRadioButton.setSelected(false);
            PIDradioButton.setSelected(true);
            streamRadioButton.setSelected(false);
            drawPIDpieChart();
        });

        programRadioButton.setOnAction( event -> {
            PIDradioButton.setSelected(false);
            programRadioButton.setSelected(true);
            streamRadioButton.setSelected(false);
            drawProgramPieChart();
        });

        streamRadioButton.setOnAction( event -> {
            PIDradioButton.setSelected(false);
            streamRadioButton.setSelected(true);
            programRadioButton.setSelected(false);
            drawStreamPieChart();
        });

        for (final PieChart.Data data : ((PieChart)chart).getData()) {

//            final Tooltip t = new Tooltip("some text");
//            data.getNode().setOnMouseEntered(new EventHandler<MouseEvent>() {
//
//                @Override
//                public void handle(MouseEvent event) {
//                    Point2D p = data.getNode().localToScreen(event.getScreenX(), event.getScreenY()); //I position the tooltip at bottom right of the node (see below for explanation)
//                    t.show(chart, p.getX(), p.getY());
//                }
//            });
//            data.getNode().setOnMouseExited(new EventHandler<MouseEvent>() {
//
//                @Override
//                public void handle(MouseEvent event) {
//                    t.hide();
//                }
//            });

            data.getNode().setOnMousePressed( event -> {
                Tooltip tooltip = ((Tooltip)tooltips.get(data.hashCode()));
                if( ! tooltip.isActivated() ) {
                        tooltip.show(vbox, event.getScreenX(), event.getScreenY());
                    for(Object currentTooltip : tooltips.values()){
                        if(!currentTooltip.equals(tooltip)) {
                            ((Tooltip) currentTooltip).hide();
                        }
                    }
                }
            });
            data.getNode().setOnMouseReleased( event -> {
                    ((Tooltip) tooltips.get(data.hashCode())).hide();
            });
        }
    }


    private void drawProgramPieChart() {
        Map programMap = new HashMap<String,Integer>();

        int totalStreamSize = 0;
        for (Map.Entry<Integer, String> programEntry : ((Map<Integer, String>)stream.getTables().getProgramMap()).entrySet()) {
            int programSize = 0;
            for (Map.Entry<Integer, Integer> PMTentry : ((Map<Integer, Integer>) stream.getTables().getPMTmap()).entrySet()) {
                if (programEntry.getKey().equals(PMTentry.getValue())) {
                    for (Map.Entry<Integer, Integer> PIDentry : ((Map<Integer, Integer>) stream.getTables().getPIDmap()).entrySet()) {
                        if (PMTentry.getKey().equals(PIDentry.getKey())) {
                            programSize += PIDentry.getValue();
                        }
                    }
                }
            }
            totalStreamSize += programSize;
            String programName = programEntry.getValue() + " (" + programEntry.getKey() + ")";
            programMap.put(programName,programSize);
        }
        programMap.put("other data", stream.getNumOfPackets() - totalStreamSize);

        double heigth = ((Legend)pieChart.lookup(".chart-legend")).getHeight();
        pieChart = createPieChart(programMap,stream.getNumOfPackets(),heigth);
        pieChart.setTitle("Program composition");
        addListenersAndHandlers(pieChart);
        vbox = new VBox(pieChart,radioButtonHBox);
        tab.setContent(vbox);
    }


    private void drawStreamPieChart() {
        Map streamMap = new HashMap<String,Integer>();

        Set ESlist = new LinkedHashSet<Integer>();
        for(Integer key : ((Map<Integer,Integer>)stream.getTables().getESmap()).values()){
            ESlist.add(key);
        }
        int totalStreamSize = 0;
        for(Object EStype : ESlist) {
            int streamSize = 0;
            for (Map.Entry<Integer, Integer> ESentry : ((Map<Integer, Integer>) stream.getTables().getESmap()).entrySet()) {
                if (ESentry.getValue().equals(EStype)) {
                    for (Map.Entry<Integer, Integer> PIDentry : ((Map<Integer, Integer>) stream.getTables().getPIDmap()).entrySet()) {
                        if (ESentry.getKey().equals(PIDentry.getKey())) {
                            streamSize += PIDentry.getValue();
                        }
                    }
                }
            }
            totalStreamSize += streamSize;
            String streamName = getElementaryStreamDescriptor((Integer) EStype) + " (" + ((Integer) EStype).intValue() + ")";
            streamMap.put(streamName, streamSize);
        }
        streamMap.put("other data", stream.getNumOfPackets() - totalStreamSize);

        double heigth = ((Legend)pieChart.lookup(".chart-legend")).getHeight();
        pieChart = createPieChart(streamMap, stream.getNumOfPackets(),heigth);
        pieChart.setTitle("Stream types composition");
        addListenersAndHandlers(pieChart);
        vbox = new VBox(pieChart,radioButtonHBox);
        tab.setContent(vbox);
    }


    private String cutString(String string, int length) {
        return string.substring(0, Math.min(string.length(), length));
    }


    private void drawPIDpieChart() {
        Map PIDmap = new HashMap<String,Integer>();
        for(Map.Entry<Integer,Integer> PIDentry : ((Map<Integer,Integer>)stream.getTables().getPIDmap()).entrySet()) {
            PIDmap.put(String.format("0x%04X", PIDentry.getKey() & 0xFFFFF) + " (" + PIDentry.getKey().toString() + ")", PIDentry.getValue());
        }
        pieChart = createPieChart(PIDmap, stream.getNumOfPackets(),null);

        pieChart.setTitle("PID composition");
        addListenersAndHandlers(pieChart);
        vbox = new VBox(pieChart,radioButtonHBox);
        tab.setContent(vbox);
    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }
}

