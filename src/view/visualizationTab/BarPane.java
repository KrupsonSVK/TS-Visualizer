package view.visualizationTab;

import app.Config;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import model.Stream;
import model.TSpacket;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.List;


public class BarPane extends VisualizationTab implements Drawer{

    private Config config;
    ScrollPane scrollPane;
    private Scene scene;
    private Rectangle rectangle;
    private ArrayList<TSpacket> packets;
    private PacketPane packetPane;
    private LegendPane legendPane;

    private EventHandler<MouseEvent> lookingGlassOnMousePressedEventHandler, lookingGlassOnMouseDraggedEventHandler;

    private double offset = 0;
    private Menu menu;

    private double oldSceneX, oldTranslateX, xPos;
    private List<Integer> sortedPIDs;
    private Stream stream;
    private int lines;

    public BarPane(Scene scene, Config config){
        this.scene = scene;
        this.config = config;
    }


    public void createScrollPane(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, int lines){


    oldSceneX = oldTranslateX = xPos = 0;

        this.packets = packets;
        this.stream = stream;
        this.sortedPIDs = sortedPIDs;
        this.lines = lines;

        menu = new Menu();

        scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setMaxSize(scene.getWidth(), scene.getHeight() * barScrollPaneHeigthRatio);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        drawBar();
        addListenersAndHandlers();
    }


    private void drawBar() {

        Canvas barCanvas = new Canvas(scene.getWidth(), scene.getHeight());
        GraphicsContext graphicsContextBarCanvas = barCanvas.getGraphicsContext2D();

        Image barBackground = drawPacketsInBar(graphicsContextBarCanvas, barCanvas, packets, (int) scene.getWidth(), (int) scene.getHeight());
        graphicsContextBarCanvas.drawImage(barBackground, scene.getWidth(), scene.getHeight());

        double lookingGlassWidth = (miniPacketImageSize / scene.getWidth() * stream.getPackets().size());
        rectangle = drawLookingGlass(0, lookingGlassWidth, barScrollPaneHeight);

        Pane barPane = new Pane(barCanvas,rectangle);
        rectangle.toFront();
        scrollPane.setContent(barPane);
    }


    private Image drawPacketsInBar(GraphicsContext gcb, Canvas barCanvas, ArrayList<TSpacket> packets, double width, double height) {

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


    private Rectangle drawLookingGlass(int xPos, double width, double height) {

        Rectangle rectangle = new Rectangle(xPos, 0, width, height);
        rectangle.setArcHeight(4);
        rectangle.setArcWidth(4);
        rectangle.setFill(Paint.valueOf("transparent"));
        rectangle.setStroke(Paint.valueOf("black"));
        rectangle.setStrokeWidth(5);

        return rectangle;
    }


    public void addListenersAndHandlers() {

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = scene.getWidth();
            scrollPane.setMaxWidth(newWidth);
            drawBar();
            addListenersAndHandlers();
        });

        lookingGlassOnMousePressedEventHandler = mouseEvent -> {
            updateX(mouseEvent);
        };

        lookingGlassOnMouseDraggedEventHandler = mouseEvent -> {
            xPos = mouseEvent.getSceneX();
            double translate = translate(mouseEvent.getSceneX());
            double  lookingGlassMoveCoeff = miniPacketImageSize / scene.getWidth() * stream.getPackets().size() ;
            //xPos += translate;
            if (xPos >= 0 && xPos < scene.getWidth()) {
                ((Rectangle) (mouseEvent.getSource())).setTranslateX(translate);
                packetPane.drawPackets(stream, packets, sortedPIDs, -xPos * lookingGlassMoveCoeff * legendPaneMoveCoeff);
                legendPane.drawPackets(stream, packets, sortedPIDs, -xPos * lookingGlassMoveCoeff );
            }
        };

        scrollPane.setOnMouseClicked((MouseEvent mouseEvent) -> {
                    if (mouseEvent.isSecondaryButtonDown()) {
                        MenuItem item = new MenuItem("all");
                        item.setDisable(false);
                        menu.getItems().add(item);
                        menu.show();
                        if (menu.isShowing())
                            menu.hide();
                    }
                }
        );

            rectangle.setOnMousePressed(lookingGlassOnMousePressedEventHandler);
            rectangle.setOnMouseDragged(lookingGlassOnMouseDraggedEventHandler);
        }

        @Override
        public void updateX(MouseEvent mouseEvent) {
            oldSceneX = mouseEvent.getSceneX();
            oldTranslateX = ((Node) mouseEvent.getSource()).getTranslateX();
        }

        @Override
        public double translate(double sceneX) {
            return oldTranslateX + sceneX - oldSceneX;
        }

        @Override
        public void setXpos(double xpos) {
            this.xPos = xpos;
        }

        @Override
        public void setOldTranslateX(double oldTranslateX) {
            this.oldTranslateX= oldTranslateX;
        }

        @Override
        public void setOldSceneX(double oldSceneX) {
            this.oldSceneX = oldSceneX;
        }

    public void setPacketPane(PacketPane packetPane) {
        this.packetPane = packetPane;
    }

    public void setLegendPane(LegendPane legendPane) {
        this.legendPane = legendPane;
    }
    }
