package view.visualizationTab;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.config.DVB;
import model.Stream;
import model.TSpacket;

import java.util.ArrayList;
import java.util.List;

import static model.config.Config.*;


public class BarPane extends VisualizationTab implements Drawer{

    private Scene scene;
    ScrollPane scrollPane;
    Rectangle lookingGlass;
    private ContextMenu contextMenu;
    private PacketPane packetPane;
    private LegendPane legendPane;
    private EventHandler<MouseEvent> lookingGlassOnMousePressedEventHandler,lookingGlassOnMouseDraggedEventHandler;
    private Stream stream;
    private ArrayList<TSpacket> packets;
    private List sortedPIDs;
    private double oldSceneX, oldTranslateX, xPos, initDiff, lastValue;


    public BarPane(Scene scene){
        this.scene = scene;
    }


    public void createScrollPane(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, int lines){

        lastValue = initDiff = oldSceneX = oldTranslateX = xPos = 0;

        this.packets = packets;
        this.stream = stream;
        this.sortedPIDs = sortedPIDs;

        contextMenu = createContextMenu(sortedPIDs);

        scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setMaxSize(scene.getWidth(), scene.getHeight() * barScrollPaneHeigthRatio);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        drawBar(xPos);
        addListenersAndHandlers();
    }

    @Override
    public double getLookingGlassMoveCoeff() {
        return miniPacketImageSize / scene.getWidth() * stream.getPackets().size() ;
    }


    private ContextMenu createContextMenu(List<Integer> sortedPIDs) {
        ContextMenu contextMenu = new ContextMenu();
        {
            CheckMenuItem item = new CheckMenuItem("All");
            item.setSelected(true);
            contextMenu.getItems().add(item);
        }
        for(Integer pid : sortedPIDs){
            if(DVB.isPSI(pid)){
                CheckMenuItem item = new CheckMenuItem(DVB.getPacketName(pid));
                item.setDisable(true);
                item.setSelected(false);
                contextMenu.getItems().add(item);
            }
        }
        return contextMenu;
    }


    private void drawBar(double xPos) {

        Canvas barCanvas = new Canvas(scene.getWidth(), scene.getHeight());
        GraphicsContext graphicsContextBarCanvas = barCanvas.getGraphicsContext2D();

        Image barBackground = drawPacketsInBar(graphicsContextBarCanvas, barCanvas, packets, (int) scene.getWidth(), (int) scene.getHeight());
        graphicsContextBarCanvas.drawImage(barBackground, scene.getWidth(), scene.getHeight());

        lookingGlass = drawLookingGlass(xPos, getLookingGlassWidth(), barScrollPaneHeight);

        Pane barPane = new Pane(barCanvas, lookingGlass);
        lookingGlass.toFront();
        scrollPane.setContent(barPane);
    }


    private double getLookingGlassWidth() {
        return (scene.getWidth() * scene.getWidth()) / (stream.getPackets().size() * miniPacketImageSize);
    }


    private Image drawPacketsInBar(GraphicsContext gcb, Canvas barCanvas, ArrayList<TSpacket> packets, double width, double height) {

        double increment = width / packets.size();
        int widthOfBar = (int) increment == 0 ? 1 : (int) increment; //if one packet is narrower than 1px

        double xPos = 0;
        for(TSpacket packet : packets){
            if(DVB.isPSI(packet.getPID())){
                drawOneBar(gcb, packet.getPID(), (int) xPos, widthOfBar, height);
            }
            xPos += increment;
        }
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.TRANSPARENT);

        return barCanvas.snapshot(snapshotParameters, null);
    }


    private void drawOneBar(GraphicsContext gc, int type, int x, int width, double height) {
        gc.setFill(getPacketColor(type));
        gc.fillRect(x, 0, width, height);
    }


    private Rectangle drawLookingGlass(double xPos, double width, double height) {

        double arcSize = 10;
        Rectangle rectangle = new Rectangle(xPos, 0, width, height - arcSize);
        rectangle.setArcHeight(arcSize);
        rectangle.setArcWidth(arcSize);
        rectangle.setFill(Color.rgb(160,230,250,0.3));
        rectangle.setStroke(Color.rgb(90,90,90));
        rectangle.setStrokeWidth(2.5);

        return rectangle;
    }


    public void addListenersAndHandlers() {

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = scene.getWidth();
            scrollPane.setMaxWidth(newWidth);
            if( lastValue != oldValue.doubleValue()) {
                xPos *= (newValue.doubleValue() / oldValue.doubleValue());
                xPos = stayInRange(xPos);
                lastValue = oldValue.doubleValue();
            }
            drawBar(xPos);
            addListenersAndHandlers();
        });

        lookingGlassOnMousePressedEventHandler = mouseEvent -> {
            updateX(mouseEvent);
            initDiff = mouseEvent.getSceneX() - ((Rectangle)(mouseEvent.getSource())).getX();
        };

        lookingGlassOnMouseDraggedEventHandler = mouseEvent -> {

            xPos = stayInRange(mouseEvent.getSceneX());
            xPos -= initDiff;

            ((Rectangle) (mouseEvent.getSource())).setX(xPos);

            packetPane.setXpos(-xPos * getLookingGlassMoveCoeff() * legendPaneMoveCoeff);
            packetPane.drawCanvas(stream, packets, sortedPIDs, -xPos * getLookingGlassMoveCoeff() * legendPaneMoveCoeff);

            legendPane.setXpos(-xPos * getLookingGlassMoveCoeff());
            legendPane.drawCanvas(stream, packets, sortedPIDs, -xPos * getLookingGlassMoveCoeff() );
        };

        scrollPane.setOnMouseClicked((MouseEvent mouseEvent) -> {
                    if (contextMenu.isShowing()) {
                        contextMenu.hide();
                    }
                    if (mouseEvent.getButton().name() == "SECONDARY") {
                        contextMenu.show(scrollPane,mouseEvent.getScreenX(),mouseEvent.getScreenY());
                    }
                }
        );

        lookingGlass.setOnMousePressed(lookingGlassOnMousePressedEventHandler);
        lookingGlass.setOnMouseDragged(lookingGlassOnMouseDraggedEventHandler);
    }


    public double stayInRange(double xPos) {
        if (xPos < 0) {
            return 0;
        }
        if (xPos > scene.getWidth()) {
            return scene.getWidth();
        }
        return xPos;
    }

    @Override
    public void drawCanvas(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {

    }

    @Override
    public void drawPackets(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {

    }

    @Override
    public void updateX(MouseEvent mouseEvent) {
        oldSceneX = mouseEvent.getSceneX();
        oldTranslateX = ((Rectangle) mouseEvent.getSource()).getX();
    }

    @Override
    public double translate(double sceneX) {
        return oldTranslateX + sceneX - oldSceneX;
    }

    @Override
    public void setXpos(double xpos) {
        this.xPos = xpos;
    }

    public void setPacketPane(PacketPane packetPane) {
        this.packetPane = packetPane;
    }

    public void setLegendPane(LegendPane legendPane) {
        this.legendPane = legendPane;
    }
}
