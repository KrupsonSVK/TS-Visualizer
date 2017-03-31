package view.visualizationTab;

import model.Config;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import model.Stream;
import model.TSpacket;
import view.Sorter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import view.Window;

import java.util.*;

public class VisualizationTab extends Window{

    public Tab tab;
    Stream stream;
    private Sorter sorter;
    private Config config;

    private Scene scene;
    private CheckBox groupByCheckBox;
    private List<Integer> sortedPIDs;
    public ArrayList<TSpacket> packets;
    private Slider zoomer;
    //private static final int mouseSensitivity = 80;

    private PacketPane packetPane;
    private BarPane barPane;
    private LegendPane legendPane;

    private EventHandler<ActionEvent> groupByCheckBoxEvent, programComboBoxEvent, zoomerEvent;
    double offset = 0;

    final static double packetImageWidth = 100;
    final static double packetImageHeight = 60;
    final static double miniPacketImageSize = 10;
    final static double typeIconSize = 19;
    final static double specialIconSize = 16;
    final static double legendPaneMoveCoeff = packetImageWidth / miniPacketImageSize;
    final static double packetScrollPaneHeightRatio = 0.54;
    final static double barScrollPaneHeigthRatio = 0.06;
    final static double legendScrollPaneHeightRatio = 0.30;
    final static double packetScrollPaneHeight = windowHeigth * packetScrollPaneHeightRatio;
    final static double fontSize = 8;
    final static double barScrollPaneHeight = windowHeigth * barScrollPaneHeigthRatio;
    final static double legendScrollPaneHeight = windowHeigth * legendScrollPaneHeightRatio;;
    final static double barHeight = windowHeigth * barScrollPaneHeigthRatio;


    public VisualizationTab() {
    }

    public VisualizationTab(Scene scene) {
        this.scene = scene;
        tab = new Tab("Visualization");
        sorter = new Sorter();
        config = new Config();

        packetPane = new PacketPane(this.scene,config);
        barPane = new BarPane(this.scene,config);
        legendPane = new LegendPane(this.scene,config);
    }


    public void visualizePackets(Stream stream) {

        this.stream = stream;

        packetPane.setLegendPane(legendPane);
        packetPane.setBarPane(barPane);
        legendPane.setPacketPane(packetPane);
        legendPane.setBarPane(barPane);
        barPane.setPacketPane(packetPane);
        barPane.setLegendPane(legendPane);

        packets = stream.getPackets();
        HashMap originalPIDmaps = new HashMap<>(stream.getPIDs());
        Map<Integer, Integer> sortedMapPIDs = new LinkedHashMap<>(sorter.sortHashMap(originalPIDmaps));
        sortedPIDs = sorter.sortPIDs(originalPIDmaps);

        packetPane.createScrollPane(stream, packets, sortedPIDs, stream.getPIDs().size());
        barPane.createScrollPane(stream, packets, sortedPIDs, stream.getPIDs().size());
        legendPane.createScrollPane(stream, packets, sortedPIDs, stream.getPIDs().size());

        packetPane.drawCanvas(stream, packets,sortedPIDs,0);
        legendPane.drawCanvas(stream, packets,sortedPIDs, 0);

        legendPane.createLabels(sortedMapPIDs);
        HBox labelsLegendScrollPaneBox = new HBox(legendPane.labelScrollPane, legendPane.scrollPane);

        HBox comboCheckboxBar = createComboCheckBoxBar(stream);

        VBox mainVBox = new VBox(comboCheckboxBar, packetPane.scrollPane, barPane.scrollPane, labelsLegendScrollPaneBox);

        VBox.setMargin(packetPane.scrollPane, new Insets(5, 5, 0, 5));
        VBox.setMargin(barPane.scrollPane, new Insets(0, 5, 0, 5));
        VBox.setMargin(labelsLegendScrollPaneBox, new Insets(0, 5, 5, 5));

        addListenersAndHandlers();

        tab.setContent(mainVBox);
    }


    private HBox createComboCheckBoxBar(Stream stream) {

        ComboBox<String> filterComboBox = createFilterComboBox(stream);
        groupByCheckBox = new CheckBox("Group by programmes");
        groupByCheckBox.setOnAction(groupByCheckBoxEvent);

        Label filterLabel = new Label("Filter:");
        Label zoomerLabel = new Label("Zoom:");

        zoomer = new Slider();
        zoomer.setMin(0);
        zoomer.setMax(100);
        zoomer.setValue(50);

        HBox comboCheckboxBar = new HBox(filterLabel, filterComboBox, groupByCheckBox, zoomerLabel, zoomer);
        HBox.setMargin(filterComboBox, new Insets(5, 5, 5, 5));
        HBox.setMargin(groupByCheckBox, new Insets(10, 35, 5, 35));
        HBox.setMargin(filterLabel, new Insets(10, 5, 5, 5));
        HBox.setMargin(zoomerLabel, new Insets(10, 5, 5, 50));
        HBox.setMargin(zoomer, new Insets(10, 5, 5, 5));

        return comboCheckboxBar;
    }


    private ComboBox<String> createFilterComboBox(Stream stream) {

        ComboBox<String> comboBox = new ComboBox<String>();
        comboBox.getItems().add("All");
        comboBox.getSelectionModel().selectFirst();

        for (Object entry : stream.getPrograms().values()) {
            comboBox.getItems().add(entry.toString());
        }
        return comboBox;
    }


    public boolean isInViewport(Scene scene, double packetPosition, double start) {
        double end = start + scene.getWidth();
        return packetPosition >= start && packetPosition <= end;
    }


    private void addListenersAndHandlers() {

        groupByCheckBoxEvent = event -> {
            if (groupByCheckBox.isSelected()) {
                System.out.print("Zgrupene");
                //TODO combobox change
                //packetScrollPane = createPacketScrollPane(packets, sortedPIDs, stream.getPrograms().size());
            } else {
                System.out.print("Odgrupene");
                //TODO combobox change
                //packetScrollPane = createPacketScrollPane(packets, sortedPIDs, stream.getPrograms().size());
            }
        };

        programComboBoxEvent = event -> {
            System.out.print("volim programovy filter");
        };

        zoomer.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                packetPane.scrollPane.setScaleX( 1 + ((new_val.doubleValue()-50) / 50));
                packetPane.scrollPane.setScaleY( 1 + ((new_val.doubleValue()-50) / 50));            }
        });
    }
}
