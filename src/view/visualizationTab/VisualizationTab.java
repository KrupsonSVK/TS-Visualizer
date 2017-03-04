package view.visualizationTab;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.util.*;
import java.util.List;

import app.Config;
import model.TSpacket;
import model.Stream;
import view.Sorter;

import static view.visualizationTab.PacketPane.packetImageWidth;


public class VisualizationTab {

    public Tab tab;
    private Stream stream;
    private Sorter sorter;

    private Scene scene;

    private ScrollPane legendScrollPane, barScrollPane, labelScrollPane;
    private CheckBox groupByCheckBox;
    private Rectangle rect;
    private List sortedPIDs;
    private ArrayList<Image> images;
    private ArrayList<TSpacket> packets;
    private double oldSceneX, oldTranslateX, oldPacketSceneX, oldPacketTranslateX;

    private EventHandler<MouseEvent> lookingGlassOnMousePressedEventHandler, lookingGlassOnMouseDraggedEventHandler;
    private EventHandler<MouseEvent> paneMousePressedEventHandler, paneMouseDraggedEventHandler, tooltipEventHadler;
    private EventHandler<ActionEvent> groupByCheckBoxEvent, programComboBoxEvent;

    private Config config;

    private final static double lookingGlassMoveCoeff = 1;
    private final static double miniPacketImageSize = 10;

    private  static final double barScrollPaneHeigthRatio = 0.06;
    private  static final double legendScrollPaneHeigthRatio = 0.30;

    private static final int mouseSensitivity = 80;

    private double xPos;
    private double offset = 0;
    private double lookingGlassWidth = 20;
    private Pane legendPane;
    private Canvas legendCanvas;

    private PacketPane packetPane;

    public VisualizationTab(Scene scene) {
        this.scene = scene;
        tab = new Tab("Visualization");
        sorter = new Sorter();
        config = new Config();
        packetPane = new PacketPane(this.scene,config);
    }

    public VisualizationTab() {
    }


    public void visualizePackets(Stream stream) {

        this.stream = stream;

        oldSceneX = oldTranslateX = oldPacketTranslateX = oldTranslateX = xPos = 0;

        packets = stream.getPackets();
        HashMap originalPIDmaps = new HashMap<>(stream.getPIDs());
        Map<Integer, Integer> sortedMapPIDs = new LinkedHashMap<>(sorter.sortHashMap(originalPIDmaps));
        sortedPIDs = sorter.sortPIDs(originalPIDmaps);

        packetPane.createPacketScrollPane(stream, packets, sortedPIDs, stream.getPIDs().size());
        createBarScrollPane(packets);
        createLegendScrollPane(packets, sortedPIDs, stream.getPIDs().size());

        packetPane.drawPacketCanvas(stream, packets,sortedPIDs,0);
        drawLegendCanvas(stream, packets,sortedPIDs, 0);

        VBox labelVBox = createLegendLabels(sortedMapPIDs);
        HBox labelsLegendScrollPaneBox = new HBox(labelVBox, legendScrollPane);
        HBox comboCheckboxBar = createComboCheckBoxBar(stream);

        ComboBox filterComboBox = createFilterComboBox(stream);
        Label filterLabel = new Label("Filter:");

        HBox filterHBox = new HBox(filterLabel, filterComboBox, barScrollPane);
        filterHBox.setMargin(filterComboBox, new Insets(5, 5, 5, 5));
        filterHBox.setMargin(filterLabel, new Insets(10, 5, 5, 5));

        VBox mainVBox = new VBox(comboCheckboxBar, packetPane.scrollPane, barScrollPane, labelsLegendScrollPaneBox);

        mainVBox.setMargin(packetPane.scrollPane, new Insets(5, 5, 0, 5));
        mainVBox.setMargin(barScrollPane, new Insets(0, 5, 0, 5));
        mainVBox.setMargin(labelsLegendScrollPaneBox, new Insets(0, 5, 5, 5));

        addListenersAndHandlers();

        tab.setContent(mainVBox);
    }




    void drawLegendCanvas(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {

        drawMiniPackets(stream, packets, sortedPIDs,xPos);

        legendPane.getChildren().clear();
        legendPane.getChildren().add(legendCanvas);
    }


    private void drawMiniPackets(Stream stream, ArrayList<TSpacket> packets, List sorted, double xPos) {

        GraphicsContext graphicsContextLegendCanvas = legendCanvas.getGraphicsContext2D();

        graphicsContextLegendCanvas.clearRect(0, 0, legendCanvas.getWidth(), legendCanvas.getHeight());
        graphicsContextLegendCanvas.setFill(Color.WHITE);
        graphicsContextLegendCanvas.fillRect(0,0, legendCanvas.getWidth(), legendCanvas.getHeight());

        int index = 0;
        for (TSpacket packet : packets) {
            if(isInViewport(index * miniPacketImageSize,(-1) * xPos)) {
                int pid = packet.getPID();
                drawMiniPacket(graphicsContextLegendCanvas, pid,  xPos + index * miniPacketImageSize, sorted.indexOf(pid));
            }
            index++;
        }
       // return legendCanvas;
    }


    boolean isInViewport(double packetPosition, double start) {
        double end = start + scene.getWidth();
        return packetPosition >= start && packetPosition <= end;
    }


    private void createBarScrollPane(ArrayList<TSpacket> sourcePIDs) {

        Canvas barCanvas = new Canvas(scene.getWidth(), scene.getHeight());
        GraphicsContext graphicsContextBarCanvas = barCanvas.getGraphicsContext2D();

        Image barBackground = drawBar(graphicsContextBarCanvas, barCanvas, sourcePIDs, (int) scene.getWidth(), (int) scene.getHeight());
        graphicsContextBarCanvas.drawImage(barBackground, scene.getWidth(), scene.getHeight());

        barScrollPane = new ScrollPane();
        barScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        barScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        barScrollPane.setMaxSize(scene.getWidth(), scene.getHeight() * barScrollPaneHeigthRatio);
        barScrollPane.setFitToHeight(true);
        barScrollPane.setFitToWidth(true);

        rect = drawLookingGlass(graphicsContextBarCanvas, 0, lookingGlassWidth, barScrollPane.getPrefHeight());

        Pane barPane = new Pane(barCanvas,rect);
        barScrollPane.setContent(barPane);

    }


    private void createLegendScrollPane(ArrayList<TSpacket> packets, List sorted, int lines) {

        legendPane = new Pane();
        legendPane.setMaxSize(scene.getWidth(),scene.getHeight());

        double legendScrollPaneHeight = scene.getHeight()*legendScrollPaneHeigthRatio;

        legendScrollPane = new ScrollPane(legendPane);
        legendScrollPane.setMaxSize(scene.getWidth(),legendScrollPaneHeight);
        legendScrollPane.setMinHeight(legendScrollPaneHeight);
        legendScrollPane.setPannable(true);
        legendScrollPane.setFitToWidth(true);
        legendScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        legendScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        double canvasHeigth = lines * miniPacketImageSize;
        if(canvasHeigth < legendScrollPaneHeight)
            canvasHeigth = legendScrollPaneHeight;

        legendCanvas = new Canvas(scene.getWidth(), canvasHeigth);
    }


    private VBox createLegendLabels(Map<Integer, Integer> PIDs) {

        VBox vBox = new VBox();
        double fontSize = 11.5;
        int labelLength = 7;

        for (Map.Entry<Integer, Integer> pid : PIDs.entrySet()) {
            Label label = new Label("PID: " + pid.getKey().toString());
            label.setFont(new Font(fontSize));
            //label.setMinWidth(lineSize*7);
            vBox.setMargin(label, new Insets(0, 0, 0, 8));
            vBox.getChildren().add(label);
        }
        labelScrollPane = new ScrollPane();
        labelScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        labelScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        labelScrollPane.setContent(vBox);
        labelScrollPane.setMinWidth(fontSize * labelLength);
        labelScrollPane.setMaxHeight(scene.getHeight()*legendScrollPaneHeigthRatio);
        labelScrollPane.setMinHeight(scene.getHeight()*legendScrollPaneHeigthRatio);

        return new VBox(labelScrollPane);
    }


    private HBox createComboCheckBoxBar(Stream stream) {

        ComboBox filterComboBox = createFilterComboBox(stream);
        groupByCheckBox = new CheckBox("Group by programmes");
        groupByCheckBox.setOnAction(groupByCheckBoxEvent);

        Label filterLabel = new Label("Filter:");

        HBox comboCheckboxBar = new HBox(filterLabel, filterComboBox, groupByCheckBox);
        comboCheckboxBar.setMargin(filterComboBox, new Insets(5, 5, 5, 5));
        comboCheckboxBar.setMargin(groupByCheckBox, new Insets(10, 35, 5, 35));
        comboCheckboxBar.setMargin(filterLabel, new Insets(10, 5, 5, 5));

        return comboCheckboxBar;
    }


    private ComboBox createFilterComboBox(Stream stream) {

        ComboBox comboBox = new ComboBox();
        comboBox.getItems().add("All");
        comboBox.getSelectionModel().selectFirst();

        for (Object entry : stream.getPrograms().values())
            comboBox.getItems().add(entry.toString());
        return comboBox;
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

        };

//        packetScrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> {
//            legendScrollPane.setHvalue(packetScrollPane.getHvalue());
//            rect.setX(getNewX(newValue, oldValue));
//        });
//
//        legendScrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> {
//            packetScrollPane.setHvalue(legendScrollPane.getHvalue());
//            rect.setX(getNewX(newValue, oldValue));
//        });

        legendScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) ->
                labelScrollPane.setVvalue(legendScrollPane.getVvalue()));

//        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
//            packetScrollPane.setPrefHeight(packetCanvas.getHeight() + scene.getHeight() - packetScrollPaneOldHeight);
//            packetScrollPaneOldHeight = scene.getHeight();
//        });

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = scene.getWidth();

            legendCanvas.setWidth(newWidth);
            legendPane.setMaxWidth(newWidth);
            legendScrollPane.setMaxWidth(newWidth);

            packetPane.drawPacketCanvas(stream, packets,sortedPIDs, xPos);
            drawLegendCanvas(stream, packets,sortedPIDs, xPos);

        });

//        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
//            packetScrollPane.setMaxHeight(scene.getHeight() - legendScrollPane.getHeight() - barScrollPane.getHeight());
//            drawPacketCanvas(stream, packets,sortedPIDs, xPos);
//            drawLegendCanvas(stream, packets,sortedPIDs, xPos);
//
//        });
//
//        paneMousePressedEventHandler = mouseEvent -> {
//            updateX(mouseEvent);
//            if(tooltip.isShowing())
//                tooltip.hide();
//        };

        paneMouseDraggedEventHandler = mouseEvent -> {
            double translate = oldPacketTranslateX + mouseEvent.getSceneX() - oldPacketSceneX;
            xPos += translate;// / mouseSensitivity;
            if(xPos > 0)
                xPos = 0;

            packetPane.drawPacketCanvas(stream, packets,sortedPIDs, xPos);
            drawLegendCanvas(stream, packets,sortedPIDs, xPos);
            updateX(mouseEvent);
        };


        lookingGlassOnMousePressedEventHandler = mouseEvent -> {
            oldSceneX = mouseEvent.getSceneX();
            oldTranslateX = ((Rectangle) mouseEvent.getSource()).getTranslateX();

        };

        lookingGlassOnMouseDraggedEventHandler = mouseEvent -> {
            double mousePosX = mouseEvent.getSceneX();
            if (mousePosX > lookingGlassWidth && mousePosX< barScrollPane.getWidth()) {
                double translate = oldTranslateX + mouseEvent.getSceneX() - oldSceneX;
                ((Rectangle) (mouseEvent.getSource())).setTranslateX(translate);
            }
        };


        legendPane.setOnMousePressed(paneMousePressedEventHandler);
        legendPane.setOnMouseDragged(paneMouseDraggedEventHandler);

        rect.setOnMousePressed(lookingGlassOnMousePressedEventHandler);
        rect.setOnMouseDragged(lookingGlassOnMouseDraggedEventHandler);
    }


    void updateX(MouseEvent mouseEvent) {
        oldPacketSceneX = mouseEvent.getSceneX();
        oldPacketTranslateX = ((Pane) mouseEvent.getSource()).getTranslateX();
    }


    String getPacketInfo(Rectangle node) {
        int index = 0;
        for(TSpacket packet : packets) {
            if (index == (int) node.getX())
                return packet.getPayload().toString();
            index+= packetImageWidth-25+offset;
        }
        return "No details found";
    }


    private double getNewX(Number newValue, Number oldValue) {
        return rect.getX() + ((double) newValue - (double) oldValue);// lookingGlassMoveCoeff;
    }


    private Image drawBar(GraphicsContext gcb, Canvas barCanvas, ArrayList<TSpacket> packets, double width, double height) {

        int type = 0    ;
        double increment = width / packets.size();
        int widthOfBar = (int) increment == 0 ? 1 : (int) increment; //ak je jeden paket na liste uzsi ako 1px

        double xPos = 0;
        for(TSpacket packet : packets){
            if(config.isPSI(packet.getPID())){
                drawOneBar(gcb, packet.getPID(), (int) xPos, widthOfBar, height);
            }
            xPos += increment;
        }
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.TRANSPARENT);

        return barCanvas.snapshot(snapshotParameters, null);
    }


    private void drawOneBar(GraphicsContext gc, int type, int x, int width, double height) {
        gc.setFill(config.getPacketColor(type));
        gc.fillRect(x, 0, width, height);
    }


    private Rectangle drawLookingGlass(GraphicsContext graphicsContext, int x, double width, double height) {

        Rectangle rectangle = new Rectangle(x, 0, width, height);
        rectangle.setFill(Paint.valueOf("transparent"));
        rectangle.setStroke(Paint.valueOf("black"));
        rectangle.setStrokeWidth(5);

        return rectangle;
    }


    private void drawMiniPacket(GraphicsContext graphicsContext, int type, double x, double y) {
        final int offset = 2;
        graphicsContext.setFill(config.getPacketColor(type));
        graphicsContext.fillRect(x + offset, y * miniPacketImageSize + offset , miniPacketImageSize-offset, miniPacketImageSize-offset); //x,y,height, width, archeigth, arcwidh
    }
}
