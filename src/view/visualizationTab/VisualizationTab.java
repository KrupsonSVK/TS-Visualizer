package view.visualizationTab;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.MapHandler;
import model.Stream;
import model.packet.Packet;
import view.Window;

import java.util.*;

import static app.Main.localization;
import static model.MapHandler.*;
import static model.config.Config.packetDisplayOffset;
import static model.config.Config.visualizationTabInsets;
import static model.config.MPEG.*;


public class VisualizationTab extends Window{

    public Tab tab;
    Stream stream;
    private MapHandler mapHandler;
    private Scene scene;
    private CheckBox groupByCheckBox;
    private ComboBox<String> filterComboBox;
    Map sortedPIDs;
    private HashMap originalPIDmap;
    private Map filteredPIDs;
    public ArrayList<Packet> packets;
    private Slider zoomer;
    private PacketPane packetPane;
    private BarPane barPane;
    private LegendPane legendPane;
    private EventHandler<ActionEvent> groupByCheckBoxEvent, filterComboBoxEvent, zoomerEvent;

    public VisualizationTab() {
        super();
        tab = new Tab(localization.getVisualizationTabText());
    }


    public void init(Scene scene){
        this.scene = scene;
        packetPane = new PacketPane(this.scene);
        barPane = new BarPane(this.scene);
        legendPane = new LegendPane(this.scene);
    }


    public void visualizePackets(Stream stream) {

        this.stream = stream;

        originalPIDmap = new HashMap<>(stream.getTables().getPIDmap());

        packetPane.setLegendPane(legendPane);
        packetPane.setBarPane(barPane);
        legendPane.setPacketPane(packetPane);
        legendPane.setBarPane(barPane);
        barPane.setPacketPane(packetPane);
        barPane.setLegendPane(legendPane);

        packets = stream.getTables().getPackets();
        sortedPIDs = ungroup(originalPIDmap);

        packetPane.createScrollPane(stream, packets, sortedPIDs, stream.getTables().getPIDmap().size());
        barPane.createScrollPane(stream, packets, sortedPIDs, stream.getTables().getPIDmap().size());
        legendPane.createScrollPane(stream, packets, sortedPIDs, stream.getTables().getPIDmap().size());

        packetPane.drawCanvas(stream, packets,0);
        legendPane.drawCanvas(stream, packets,0);

        Map<Integer, Integer> sortedMapPIDs = new LinkedHashMap<>(sortHashMapByKey(sortedPIDs));
        Map labeledPIDs = createLabeledPIDs(sortedMapPIDs, null, null, null, null);
        legendPane.createLabels(labeledPIDs);

        HBox labelsLegendScrollPaneBox = new HBox(legendPane.labelScrollPane, legendPane.scrollPane);

        HBox comboCheckboxBar = createFilterControls(stream);

        VBox mainVBox = new VBox(comboCheckboxBar, packetPane.scrollPane, barPane.scrollPane, labelsLegendScrollPaneBox);

        VBox.setMargin(packetPane.scrollPane, new Insets(visualizationTabInsets, visualizationTabInsets, 0, visualizationTabInsets));
        VBox.setMargin(barPane.scrollPane, new Insets(0, visualizationTabInsets, 0, visualizationTabInsets));
        VBox.setMargin(labelsLegendScrollPaneBox, new Insets(0, visualizationTabInsets, visualizationTabInsets, visualizationTabInsets));

        addListenersAndHandlers();

        tab.setContent(mainVBox);
    }


    private HBox createFilterControls(Stream stream) {

        filterComboBox = createFilterComboBox(stream);
        filterComboBox.setOnAction(filterComboBoxEvent);

        groupByCheckBox = new CheckBox(localization.getGroupProgramsText());
        groupByCheckBox.setOnAction(groupByCheckBoxEvent);

        Label filterLabel = new Label(localization.getProgramFilterText());
        Label zoomerLabel = new Label("Zoom:");
        zoomerLabel.setDisable(true);

        zoomer = new Slider(0,100,50);
        zoomer.setDisable(true);

        HBox comboCheckboxBar = new HBox(filterLabel, filterComboBox, groupByCheckBox, zoomerLabel, zoomer);
        HBox.setMargin(filterComboBox, new Insets(visualizationTabInsets, visualizationTabInsets, visualizationTabInsets, visualizationTabInsets));
        HBox.setMargin(groupByCheckBox, new Insets(2* visualizationTabInsets, 7* visualizationTabInsets, visualizationTabInsets, 7* visualizationTabInsets));
        HBox.setMargin(filterLabel, new Insets(2* visualizationTabInsets, visualizationTabInsets, 2* visualizationTabInsets, visualizationTabInsets));
        HBox.setMargin(zoomerLabel, new Insets(2* visualizationTabInsets, visualizationTabInsets, visualizationTabInsets, 10* visualizationTabInsets));
        HBox.setMargin(zoomer, new Insets(2* visualizationTabInsets, visualizationTabInsets, visualizationTabInsets, visualizationTabInsets));

        return comboCheckboxBar;
    }

    protected ComboBox<String> createFilterComboBox(Stream stream) {

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().add(localization.getAllText());
        comboBox.getSelectionModel().selectFirst();

        for (Object entry : stream.getTables().getProgramMap().values()) {
            comboBox.getItems().add(entry.toString());
        }
        return comboBox;
    }


    public boolean isInViewport(Scene scene, double packetPosition, double start) {
        double end = start + scene.getWidth();
        return packetPosition >= start - packetDisplayOffset && packetPosition <= end + packetDisplayOffset;
    }


    private void addListenersAndHandlers() {

        groupByCheckBoxEvent = event -> {
            groupProgrammes(filteredPIDs,stream.getTables().getPMTmap(),stream.getTables().getProgramMap(),stream.getTables().getPATmap(),stream.getTables().getEnhancedPMTmap());
        };

        filterComboBoxEvent = (ActionEvent event) -> {
            filteredPIDs = filterProgram(filterComboBox.getValue(), stream.getTables().getPMTmap(), stream.getTables().getProgramMap());
            groupProgrammes(filteredPIDs,stream.getTables().getPMTmap(),stream.getTables().getProgramMap(),stream.getTables().getPATmap(),stream.getTables().getEnhancedPMTmap());
        };

        zoomerEvent = event -> {
            zoomer.valueProperty().addListener((ov, old_val, new_val) -> {
                //TODO implement zoomer
                packetPane.scrollPane.setScaleX(1 + ((new_val.doubleValue() - 50) / 50));
                packetPane.scrollPane.setScaleY(1 + ((new_val.doubleValue() - 50) / 50));
            });
        };

        groupByCheckBox.setOnAction(groupByCheckBoxEvent);
        filterComboBox.setOnAction(filterComboBoxEvent);
    }


    protected void groupProgrammes(Map filteredPIDs, Map PMTmap, Map programs, Map PATmap, Map enhancedPMTmap) {

        Map labeledPIDs;
        if (groupByCheckBox.isSelected()) {
            sortedPIDs = groupByProgrammes(filteredPIDs, enhancedPMTmap);
            labeledPIDs = createLabeledPIDs(null, PMTmap, sortedPIDs, programs, PATmap);
        }
        else {
            Map PIDs = (filteredPIDs != null) ? filteredPIDs : originalPIDmap;
            sortedPIDs = ungroup(PIDs);
            Map<Integer, Integer> sortedMapPIDs = new LinkedHashMap<>(sortHashMapByKey(sortedPIDs));
            labeledPIDs = createLabeledPIDs(sortedMapPIDs,null,null, null, null);
        }
        updatePanes(sortedPIDs,labeledPIDs);
    }


    private void updatePanes(Map sortedPIDs, Map labeledPIDs) {

        //legendPane.setScrollPaneHeight();
        legendPane.scrollPane.setPrefHeight(legendPane.getScrollPaneHeightCoeff());
        legendPane.labelScrollPane.setPrefHeight(legendPane.getScrollPaneHeightCoeff());

        packetPane.setSortedPIDs(sortedPIDs);
        legendPane.setSortedPIDs(sortedPIDs);
        barPane.setSortedPIDs(sortedPIDs);

        packetPane.drawPackets(stream,packets,packetPane.getXpos());
        legendPane.drawPackets(stream,packets,legendPane.getXpos());

        legendPane.createLabels(labeledPIDs);
    }


    private <K, V> Map createLabeledPIDs(Map<K, V> sortedMapPIDs, Map PMTmap, Map<K, V> gruppedPMTmap, Map programs, Map PATmap) {
        Map resultMap = new HashMap();
        if(gruppedPMTmap == null){
            for (Map.Entry<K, V> entry : sortedMapPIDs.entrySet()) {
                resultMap.put(entry.getKey(),"PID: ");
            }
        }
        else{
            Integer previous = nil;
            for (Map.Entry<K, V> entry : gruppedPMTmap.entrySet()) {
                if ( ! previous.equals(entry.getValue())){
                    Integer PID = (Integer) entry.getKey();
                    previous = (Integer) entry.getValue();

                    if(!isPSI(PID) && programs.get(PMTmap.get(PID)) != null){
                        resultMap.put(PMTmap.get(PID),"Program: ");
                    }
                    else if(isPMT(PATmap,PID)){
                        resultMap.put(PMTmap.get(PID),"PMT: ");
                    }
                    else {
                        resultMap.put(PID, "PID: ");
                    }
                }
            }
        }
        Map map = sortHashMapByKey(resultMap);
        return map;
    }


    private <K,V> Map groupByProgrammes(Map<K,V> filteredPIDs, Map<Integer,Integer> enhancedPMTmap) {

        HashMap gruppedMap = new HashMap<Integer,Integer>();

        if( filteredPIDs == null ){
            int index = nil;
            Integer previous = nil;
            for (Map.Entry<Integer,Integer> entry : enhancedPMTmap.entrySet()) {
                if (!previous.equals(entry.getValue())) {
                    previous = entry.getValue();
                    index++;
                }
                gruppedMap.put(entry.getKey(), index);
            }
        }
        else {
            for (Map.Entry<K,V> entry : filteredPIDs.entrySet()) {
                gruppedMap.put(entry.getKey(), 0);
            }
        }
        return sortHashMapByValue(gruppedMap);
    }


    private HashMap ungroup(Map originalPIDmaps) {
        List<Integer> sorted = sortMapToListByKey(originalPIDmaps);
        HashMap ungruppedMap = new HashMap<Integer,Integer>();

        for(Integer item : sorted){
            ungruppedMap.put(item,sorted.indexOf(item));
        }
        return ungruppedMap;
    }


    boolean isPMT(Map PATmap, int PID) {
        return getByValue(PATmap,PID) != null;
    }
}

