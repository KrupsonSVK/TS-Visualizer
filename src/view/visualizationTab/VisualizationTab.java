package view.visualizationTab;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import model.Stream;
import model.TSpacket;
import model.Sorter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import view.Window;

import java.util.*;

import static model.config.Config.*;


public class VisualizationTab extends Window{

    public Tab tab;
    Stream stream;
    private Sorter sorter;
    private Scene scene;
    private CheckBox groupByCheckBox;
    List<Integer> sortedPIDs;
    public ArrayList<TSpacket> packets;
    private Slider zoomer;
    private PacketPane packetPane;
    private BarPane barPane;
    LegendPane legendPane;
    private EventHandler<ActionEvent> groupByCheckBoxEvent, programComboBoxEvent, zoomerEvent;

    public VisualizationTab() {
        super();
        tab = new Tab("Visualization");
        sorter = new Sorter();
    }


    public void init(Scene scene){
        this.scene = scene;

        packetPane = new PacketPane(this.scene);
        barPane = new BarPane(this.scene);
        legendPane = new LegendPane(this.scene);
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
        sortedPIDs = sorter.sortMapToListByKey(originalPIDmaps);

        packetPane.createScrollPane(stream, packets, sortedPIDs, stream.getPIDs().size());
        barPane.createScrollPane(stream, packets, sortedPIDs, stream.getPIDs().size());
        legendPane.createScrollPane(stream, packets, sortedPIDs, stream.getPIDs().size());

        packetPane.drawCanvas(stream, packets,sortedPIDs,0);
        legendPane.drawCanvas(stream, packets,sortedPIDs, 0);

        legendPane.createLabels(sortedMapPIDs);
        HBox labelsLegendScrollPaneBox = new HBox(legendPane.labelScrollPane, legendPane.scrollPane);

        HBox comboCheckboxBar = createComboCheckBoxBar(stream);

        VBox mainVBox = new VBox(comboCheckboxBar, packetPane.scrollPane, barPane.scrollPane, labelsLegendScrollPaneBox);

        VBox.setMargin(packetPane.scrollPane, new Insets(inset, inset, 0, inset));
        VBox.setMargin(barPane.scrollPane, new Insets(0, inset, 0, inset));
        VBox.setMargin(labelsLegendScrollPaneBox, new Insets(0, inset, inset, inset));

        addListenersAndHandlers();

        tab.setContent(mainVBox);
    }


    private HBox createComboCheckBoxBar(Stream stream) {

        ComboBox<String> filterComboBox = createFilterComboBox(stream);
        groupByCheckBox = new CheckBox("Group by programmes");
        groupByCheckBox.setOnAction(groupByCheckBoxEvent);

        Label filterLabel = new Label("Filter:");
        Label zoomerLabel = new Label("Zoom:");

        zoomer = new Slider(0,100,50);

        HBox comboCheckboxBar = new HBox(filterLabel, filterComboBox, groupByCheckBox, zoomerLabel, zoomer);
        HBox.setMargin(filterComboBox, new Insets(inset, inset, inset, inset));
        HBox.setMargin(groupByCheckBox, new Insets(2*inset, 7*inset, inset, 7*inset));
        HBox.setMargin(filterLabel, new Insets(2*inset, inset, inset, inset));
        HBox.setMargin(zoomerLabel, new Insets(2*inset, inset, inset, 10*inset));
        HBox.setMargin(zoomer, new Insets(2*inset, inset, inset, inset));

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
                //TODO implement zoomer
                packetPane.scrollPane.setScaleX( 1 + ((new_val.doubleValue()-50) / 50));
                packetPane.scrollPane.setScaleY( 1 + ((new_val.doubleValue()-50) / 50));
            }
        });
    }
}
