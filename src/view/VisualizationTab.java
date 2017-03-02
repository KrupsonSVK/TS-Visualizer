package view;

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
import javafx.scene.layout.StackPane;
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


public class VisualizationTab {

    public Tab tab;
    private Scene scene;
    private Pane legendPane;
    private Pane pane;
    private ScrollPane packetScrollPane;
    private ScrollPane legendScrollPane;
    private ScrollPane barScrollPane;
    private Canvas legendScrollPaneCanvas;
    private Canvas barCanvas;
    private Canvas packetPaneCanvas;
    private double packetScrollPaneOldHeight;
    private CheckBox groupByCheckBox;
    private ComboBox filterComboBox;
    private Image img;
    private Rectangle rect;
    private double oldSceneX, oldTranslateX, oldPacketSceneX, oldPacketTranslateX;
    private Sorter sorter;
    private ScrollPane labelsScrollPane;

    private ArrayList<TSpacket> packets;
    private List sortedPIDs;

    private EventHandler<MouseEvent> lookingGlassOnMousePressedEventHandler, lookingGlassOnMouseDraggedEventHandler;
    private EventHandler<ActionEvent> programComboBoxEvent;
    private EventHandler<MouseEvent> tooltipEventHadler;

    private final Config config;
    private final static double packetImageWidth = 100;
    private final static double packetImageHeigth = 60;
    private final static double lookingGlassMoveCoeff = 1;
    private final static double miniPacketImageSize = 5;

    private  static final double packetScrollPaneHeigthRatio = 0.54;
    private  static final double barScrollPaneHeigthRatio = 0.06;
    private  static final double legendScrollPaneHeigthRatio = 0.30;

    private EventHandler<MouseEvent>  packetPaneMousePressedEventHandler, packetPaneMouseDraggedEventHandler;

    private double xPos;
    private Stream stream;
    private Tooltip tooltip;
    private double offset = 0;
    private static final int mouseSensitivity = 80;

    VisualizationTab(Scene scene) {
        this.scene = scene;
        tooltip = new Tooltip();
        tab = new Tab("Visualization");
        sorter = new Sorter();
        config = new Config();
        packetScrollPaneOldHeight = scene.getHeight();
    }


    public void visualizePackets(Stream stream) {

        this.stream = stream;



        oldSceneX = oldTranslateX = oldPacketTranslateX = oldTranslateX = xPos = 0;

        packets = new ArrayList<>(stream.getPackets().subList(0, 100));
        HashMap originalPIDmaps = new HashMap<>(stream.getPIDs());
        Map<Integer, Integer> sortedMapPIDs = new LinkedHashMap<>(sorter.sortHashMap(originalPIDmaps));
        sortedPIDs = sorter.sortPIDs(originalPIDmaps);

        createPacketScrollPane(packets, sortedPIDs, stream.getPIDs().size());
        barScrollPane = createBarScrollPane(packets, stream);
        legendScrollPane = createLegendScrollPane(packets, sortedPIDs, stream.getPIDs().size());

        drawPacketCanvas(stream, packets,sortedPIDs,0);

        addListenersAndHandlers();

        VBox labelVbox = createLegendLabels(sortedMapPIDs);
        HBox labelsLegendScrollPaneBox = new HBox(labelVbox, legendScrollPane);

        HBox comboCheckboxBar = createComboCheckBoxBar(stream);

        VBox mainVbox = new VBox(comboCheckboxBar, packetScrollPane, barScrollPane, labelsLegendScrollPaneBox);
        mainVbox.setMargin(packetScrollPane, new Insets(5, 5, 0, 5));
        mainVbox.setMargin(barScrollPane, new Insets(0, 5, 0, 5));
        mainVbox.setMargin(labelsLegendScrollPaneBox, new Insets(0, 5, 5, 5));

        tab.setContent(mainVbox);
    }


    private void drawPacketCanvas(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {

        packetPaneCanvas = drawPackets(stream, packets, sortedPIDs,xPos);

        pane.getChildren().clear();
        pane.getChildren().add(packetPaneCanvas);
    }


    private void createPacketScrollPane(ArrayList<TSpacket> packets, List sorted, int lines) {

        pane = new Pane();
        pane.setMaxSize(scene.getWidth(),scene.getHeight());

        packetScrollPane = new ScrollPane();
        packetScrollPane.setMaxSize(scene.getWidth(),scene.getHeight() * packetScrollPaneHeigthRatio);//54%
        packetScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        packetScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        packetScrollPane.setPannable(true);
        packetScrollPane.setFitToWidth(true);
        packetScrollPane.setContent(pane);

        double canvasHeigth = lines * packetImageHeigth;
        if(canvasHeigth < packetScrollPane.getMaxHeight())
            canvasHeigth = packetScrollPane.getMaxHeight();

        packetPaneCanvas = new Canvas(scene.getWidth(), canvasHeigth);
    }


    private Canvas drawPackets(Stream stream, ArrayList<TSpacket> packets, List sorted, double xPos) {

        GraphicsContext graphicsContextPacketCanvas = packetPaneCanvas.getGraphicsContext2D();
        graphicsContextPacketCanvas.clearRect(0, 0, packetPaneCanvas.getWidth(), packetPaneCanvas.getHeight());

        graphicsContextPacketCanvas.setFill(Color.WHITE);
        graphicsContextPacketCanvas.fillRect(0,0,scene.getWidth(),scene.getHeight());

        int index = 0;
        for (TSpacket packet : packets) {
            if(isInViewport(index++,xPos)) {
                int pid = packet.getPID();
                drawPacketImg(graphicsContextPacketCanvas, pid, pid, config.getProgramName(stream,pid), sorted.indexOf(pid), xPos+index);
            }

        }
        return packetPaneCanvas;
    }


    private boolean isInViewport(int index, double start) {
        double end = start + scene.getWidth();
        double packetPosition = index * 100;
        return packetPosition >= start - 1000 && packetPosition <= end + 1000;
    }


    private HBox createComboCheckBoxBar(Stream stream) {

        filterComboBox = createProgramComboBox(stream);
        groupByCheckBox = new CheckBox("Group by programmes");
        groupByCheckBox.setOnAction(programComboBoxEvent);

        Label filterLabel = new Label("Filter:");

        HBox comboCheckboxBar = new HBox(filterLabel, filterComboBox, groupByCheckBox);
        comboCheckboxBar.setMargin(filterComboBox, new Insets(5, 5, 5, 5));
        comboCheckboxBar.setMargin(groupByCheckBox, new Insets(10, 35, 5, 35));
        comboCheckboxBar.setMargin(filterLabel, new Insets(10, 5, 5, 5));

        return comboCheckboxBar;
    }


    private ComboBox createProgramComboBox(Stream stream) {

        ComboBox comboBox = new ComboBox();
        comboBox.getItems().add("All");
        comboBox.getSelectionModel().selectFirst();

        for (Object entry : stream.getPrograms().values())
            comboBox.getItems().add(entry.toString());
        return comboBox;
    }


    private ScrollPane createBarScrollPane(ArrayList<TSpacket> sourcePIDs, Stream stream) {

        ScrollPane barScrollPane = new ScrollPane();
        barScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        barScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        barScrollPane.setPrefHeight(scene.getHeight() * barScrollPaneHeigthRatio);
        barScrollPane.setFitToHeight(true);
        barScrollPane.setFitToWidth(true);

        barCanvas = new Canvas(scene.getWidth(), scene.getHeight());
        GraphicsContext graphicsContextBarCanvas = barCanvas.getGraphicsContext2D();
        barScrollPane.setContent(barCanvas);
        img = drawBar(graphicsContextBarCanvas, barCanvas, stream.getNumOfPackets(), (int) scene.getWidth(), (int) scene.getHeight());
        graphicsContextBarCanvas.drawImage(img, scene.getWidth(), scene.getHeight());

        rect = drawLookingGlass(graphicsContextBarCanvas, 0, 20, barScrollPane.getPrefHeight());
        rect.setOnMousePressed(lookingGlassOnMousePressedEventHandler);
        rect.setOnMouseDragged(lookingGlassOnMouseDraggedEventHandler);
        rect.toFront();

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(rect);
        stackPane.toFront();
        rect.toFront();
        barScrollPane.setContent(stackPane);

        return barScrollPane;
    }


    private ScrollPane createLegendScrollPane(ArrayList<TSpacket> packets, List sorted, int lines) {

        double canvasWidth = 500 * miniPacketImageSize;
        double canvasHeigth = 100 * miniPacketImageSize;

        legendPane = new Pane();
        legendPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        legendPane.setStyle("-fx-background-color: white");

        legendScrollPane = new ScrollPane(legendPane);
        legendScrollPane.setPrefSize(canvasWidth,scene.getHeight() * legendScrollPaneHeigthRatio);
        legendScrollPane.setPannable(true);
        legendScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        legendScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        legendScrollPaneCanvas = new Canvas(canvasWidth, canvasHeigth);
        GraphicsContext graphicsContextLegendPane = legendScrollPaneCanvas.getGraphicsContext2D();
        legendPane.getChildren().add(legendScrollPaneCanvas);

        int xPos = 0;
        for (TSpacket pid : packets)
            drawMiniPacket(graphicsContextLegendPane, pid.getPID(), pid.getPID(), xPos++, sorted.indexOf(pid.getPID()));

        return legendScrollPane;
    }


    private VBox createLegendLabels(Map<Integer, Integer> PIDs) {

        VBox vBox = new VBox();
        int fontSize = 11;
        int labelLength = 7;

        for (Map.Entry<Integer, Integer> pid : PIDs.entrySet()) {
            Label label = new Label("PID: " + pid.getKey().toString());
            label.setFont(new Font(fontSize));
            //label.setMinWidth(lineSize*7);
            vBox.setMargin(label, new Insets(0, 0, 0, 8));
            vBox.getChildren().add(label);
        }
        labelsScrollPane = new ScrollPane();
        labelsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        labelsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        labelsScrollPane.setContent(vBox);
        labelsScrollPane.setMinWidth(fontSize * labelLength);

        return new VBox(labelsScrollPane);
    }


    private void addListenersAndHandlers() {

        programComboBoxEvent = event -> {
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
                labelsScrollPane.setVvalue(legendScrollPane.getVvalue()));

//        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
//            packetScrollPane.setPrefHeight(packetPaneCanvas.getHeight() + scene.getHeight() - packetScrollPaneOldHeight);
//            packetScrollPaneOldHeight = scene.getHeight();
//        });

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            packetScrollPane.setMaxWidth(scene.getWidth());
            pane.setMaxWidth(scene.getWidth());
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            //packetScrollPane.setMaxHeight(scene.getHeight() * packetScrollPaneHeigthRatio);//54%
        });

        packetPaneMousePressedEventHandler = mouseEvent -> {
            updateX(mouseEvent);

            if(tooltip.isShowing())
                tooltip.hide();
            System.out.println("packetPaneMousePressedEventHandler " + mouseEvent.getSceneX() );

        };

        packetPaneMouseDraggedEventHandler = mouseEvent -> {
            System.out.println("packetPaneMouseDraggedEventHandler " + mouseEvent.getSceneX() );

            double translate = oldPacketTranslateX + mouseEvent.getSceneX() - oldPacketSceneX;

            xPos += translate / mouseSensitivity;
            if(xPos > 0)
                xPos = 0;

            drawPacketCanvas(stream, packets,sortedPIDs, xPos);
            updateX(mouseEvent);
        };


        lookingGlassOnMousePressedEventHandler = mouseEvent -> {
            oldSceneX = mouseEvent.getSceneX();
            oldTranslateX = ((Rectangle) mouseEvent.getSource()).getTranslateX();
            System.out.println("oldSceneX: " + oldSceneX + "; oldTranslateX:" + oldTranslateX);
        };

        lookingGlassOnMouseDraggedEventHandler = mouseEvent -> {
            double translate = oldTranslateX + mouseEvent.getSceneX() - oldSceneX;
            ((Rectangle) (mouseEvent.getSource())).setTranslateX(translate);
            System.out.println("translateX: " + translate);
        };

        tooltipEventHadler = mouseEvent -> {
            Rectangle rectangle = (Rectangle) mouseEvent.getSource();
            tooltip.setText(getPacketInfo(rectangle));
            tooltip.show(rectangle, mouseEvent.getScreenX() + 50, mouseEvent.getScreenY());
        };

        pane.setOnMousePressed(packetPaneMousePressedEventHandler);
        pane.setOnMouseDragged(packetPaneMouseDraggedEventHandler);
    }

    private void updateX(MouseEvent mouseEvent) {
        oldPacketSceneX = mouseEvent.getSceneX();
        oldPacketTranslateX = ((Pane) mouseEvent.getSource()).getTranslateX();
    }


    private String getPacketInfo(Rectangle node) {
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


    private Image drawBar(GraphicsContext gcb, Canvas barCanvas, double numOfPackets, double width, double height) {

        double increment = width / numOfPackets;
        int widthOfBar = (int) increment == 0 ? 1 : (int) increment;

        int previousPos = config.nil;
        for (double xPos = 0; xPos < width-200; xPos += increment) {

            if ((int) xPos > previousPos)
                drawOneBar(gcb, (int) (Math.random() * 3), (int) xPos, widthOfBar, height); //TODO type ako color

            previousPos = (int) xPos;
        }
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.TRANSPARENT);

        return barCanvas.snapshot(snapshotParameters, null);
    }


    private void drawPacketImg(GraphicsContext graphicsContext, int type, int pid, String name, int yPos, double xPos) {

        xPos *= packetImageWidth-25;
        yPos *= packetImageHeigth;

        javafx.scene.image.Image original = new javafx.scene.image.Image(getClass().getResourceAsStream("/app/resources/" + config.getPacketImageName(type))); //TODO toto musia byt array of finals
        graphicsContext.drawImage(original, xPos, yPos, packetImageWidth, packetImageHeigth);
        graphicsContext.setFont(new Font(8));
        graphicsContext.strokeText("PID: " + pid + "\n" + config.getPacketName(pid) + "\n" + name, xPos + 5, yPos + 30);

        Rectangle rectangle = new Rectangle(xPos, yPos, packetImageWidth-10, packetImageHeigth);
        rectangle.setFill(Paint.valueOf("transparent"));
        rectangle.setOnMouseClicked(tooltipEventHadler);

        pane.getChildren().add(rectangle);
    }


    private void drawMiniPacket(GraphicsContext gc, int type, Integer pid, int x, int y) {
        gc.setFill(config.getPacketColor(type));
        gc.fillRect(x * 12, y * 15 , 10, 10); //x,y,height, width, archeigth, arcwidh
    }


    private void drawOneBar(GraphicsContext gc, int type, int x, int width, double height) {
        gc.setFill(config.getPacketColor(type));
        gc.fillRect(x, 0, width, height);
    }


    private Rectangle drawLookingGlass(GraphicsContext gc, int x, double width, double height) {

        gc.setFill(Color.DARKGREY);
        gc.setLineWidth(3);

        Rectangle rectangle = new Rectangle(x, 0, width, height);
        rectangle.setFill(Paint.valueOf("transparent"));
        rectangle.setStroke(Paint.valueOf("black"));
        rectangle.setStrokeWidth(5);

        return rectangle;
    }
}
