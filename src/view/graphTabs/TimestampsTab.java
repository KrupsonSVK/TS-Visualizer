package view.graphTabs;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Stream;
import model.Tables;
import model.config.MPEG;
import view.visualizationTab.VisualizationTab;

import java.util.HashMap;
import java.util.Map;

import static app.Main.localization;
import static model.config.Config.chartHBoxInsets;
import static model.config.Config.chartInsets;
import static model.config.MPEG.*;


public class TimestampsTab extends VisualizationTab implements Graph{

    private Scene scene;
    public Tab tab;
    private Stream stream;

    private Label captionLabel;
    private ScatterChart scatterChart;
    private ComboBox<String> filterComboBox;

    private EventHandler<ActionEvent> filterComboBoxEvent;

    public static final int tickUnit = 10;
    private HBox filterHBox;

    private Map tooltips;
    private VBox vbox;


    public TimestampsTab(){
        tab = new Tab(localization.getTimestampsTabText());
        captionLabel = new Label("");
        tooltips = new HashMap();
    }


    public void drawGraph(Stream streamDescriptor) {

        this.stream = streamDescriptor;

        filterComboBox = createFilterComboBox(stream);
        filterComboBox.setValue(filterComboBox.getItems().get(filterComboBox.getItems().size() > 1 ? 1 : 0));

        filterHBox = new HBox(new Label(localization.getProgramFilterText()), filterComboBox);
        filterHBox.setAlignment(Pos.CENTER);
        filterHBox.setSpacing(10);
        filterHBox.setPadding(chartHBoxInsets);

        captionLabel.setTextFill(Color.DARKORANGE);
        captionLabel.setStyle("-fx-font: 24 arial;");
        captionLabel.toFront();

        scatterChart = createScatterChart(stream.getTables(),filterComboBox.getValue());
        addListenersAndHandlers(scatterChart);
        filterComboBox.setOnAction(filterComboBoxEvent);

        vbox = new VBox(captionLabel,scatterChart,filterHBox);
        tab.setContent(vbox);
    }


    private<K,V>ScatterChart createScatterChart(Tables tables, String selectedService) {
        tooltips.clear();
        final ScatterChart scatterChart = createScaledScatterChart(tables, selectedService);

        for (Map.Entry<Integer, String> program : ((Map<Integer, String>) tables.getProgramMap()).entrySet()) {

            if (selectedService.equals(localization.getAllText()) || program.getValue().equals(selectedService)) {
                for (Map.Entry<Integer, Map<MPEG.TimestampType, Map<Long, Long>>> serviceTimestamps : ((Map<Integer, Map<MPEG.TimestampType, Map<Long, Long>>>) tables.getServiceTimestampMap()).entrySet()) {

                    if (program.getKey().equals(serviceTimestamps.getKey())) {
                        for (Map.Entry<MPEG.TimestampType, Map<Long, Long>> timestampMap : serviceTimestamps.getValue().entrySet()) {

                            XYChart.Series series = createSeries(scatterChart.getData(),timestampMap.getKey().toString());
                            series.getData().add(new XYChart.Data(nil, nil)); //default entry for legend displaying

                            for (Map.Entry<Long, Long> packetEntry : timestampMap.getValue().entrySet()) {
                                XYChart.Data data = new XYChart.Data(packetEntry.getValue(), packetEntry.getKey());
                                series.getData().add(data);

                                tooltips.put(data.hashCode(), new Tooltip(
                                        localization.getTypeText() + timestampMap.getKey().toString() + "\nService: " + program.getKey() + " (" + localization.getPacketPositionText() + ": " + packetEntry.getValue())
                                );
                            }
                            scatterChart.setData(updateSeries(scatterChart.getData(),series));
                        }
                    }
                }
            }
        }
        scatterChart.setTitle(localization.getTimestampsLayoutText());
        scatterChart.setLegendSide(Side.LEFT);
        scatterChart.setAnimated(false);
        scatterChart.toBack();
        scatterChart.setPadding(chartInsets);
        scatterChart.setPrefHeight(scene.getHeight());

        return scatterChart;
    }


    private ObservableList<XYChart.Series> updateSeries(ObservableList<XYChart.Series> data, XYChart.Series series) {
        if(!data.contains(series)){
            data.add(series);
        }
        return data;
    }


    private XYChart.Series createSeries(ObservableList<XYChart.Series> series, String name) {
        for (XYChart.Series serie : series) {
            if (serie.getName().equals(name)) {
                return serie;
            }
        }
        XYChart.Series serie = new XYChart.Series();
        serie.setName(name);
        return serie;
    }


    private ScatterChart createScaledScatterChart(Tables tables, String selectedService) {
        Long maxX = 0L;
        Long maxY = 0L;
        for (Map.Entry<Integer, String> program : ((Map<Integer, String>) tables.getProgramMap()).entrySet()) {
            if (selectedService.equals(localization.getAllText()) || program.getValue().equals(selectedService)) {
                for (Map.Entry<Integer, Map<MPEG.TimestampType, Map<Long, Long>>> serviceTimestamps : ((Map<Integer, Map<MPEG.TimestampType, Map<Long, Long>>>) tables.getServiceTimestampMap()).entrySet()) {
                    if (program.getKey().equals(serviceTimestamps.getKey())) {
                        for (Map.Entry<MPEG.TimestampType, Map<Long, Long>> timestampMap : serviceTimestamps.getValue().entrySet()) {
                            for (Map.Entry<Long, Long> packetEntry : timestampMap.getValue().entrySet()) {
                                if (packetEntry.getValue().longValue() > maxX.longValue()) {
                                    maxX = packetEntry.getValue();
                                }
                                if (packetEntry.getKey().longValue() > maxY.longValue()) {
                                    maxY = packetEntry.getKey();
                                }
                            }
                        }
                    }
                }
            }
        }
        final NumberAxis xAxis = new NumberAxis(0, Double.valueOf(maxX*1.005f).longValue(), Double.valueOf(maxX*0.08f).longValue());
        final NumberAxis yAxis = new NumberAxis(0, Double.valueOf(maxY*1.005f).longValue(), Double.valueOf(maxY*0.08f).longValue());

        yAxis.setTickLabelFormatter(
                new NumberAxis.DefaultFormatter(yAxis) {
                    @Override
                    public String toString(Number object) {
                        //long timestamp = startTimeStamp + (tickInterval * object.intValue());
                        // String out = parseTimestamp(timestamp);
                        return String.format("%s",parseTimestamp(object.longValue()));
                    }
                });

        xAxis.setLabel(localization.getPacketOrderText());
        yAxis.setLabel(localization.getTimeText());

        return new ScatterChart<>(xAxis, yAxis);
    }


    public void addListenersAndHandlers(Chart chart) {
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            chart.setPrefHeight(scene.getHeight());
        });

        filterComboBoxEvent = (ActionEvent event) -> {
            scatterChart = createScatterChart(stream.getTables(), filterComboBox.getValue());
            vbox = new VBox(captionLabel, scatterChart, filterHBox);
            tab.setContent(vbox);
        };

        for (XYChart.Series<Number, Number> series : ((XYChart<Number, Number>) chart).getData()) {
            for (XYChart.Data<Number, Number> data : series.getData()) {
                data.getNode().setOnMousePressed(event -> {
                    Tooltip tooltip = ((Tooltip) tooltips.get(data.hashCode()));
                    if (!tooltip.isActivated()) {
                        tooltip.show(vbox, event.getScreenX(), event.getScreenY());
//                        for (Object currentTooltip : tooltips.values()) {
//                            if (!currentTooltip.equals(tooltip)) {
//                                ((Tooltip) currentTooltip).hide();
//                            }
//                        }
                    }
                });
                data.getNode().setOnMouseReleased(event -> {
//                    if (((Tooltip) tooltips.get(data.hashCode())).isActivated()) {
                    ((Tooltip) tooltips.get(data.hashCode())).hide();
//                    }
                });
            }
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}

