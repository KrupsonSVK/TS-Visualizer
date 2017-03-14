package view.visualizationTab;

import app.Config;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.Stream;
import model.TSpacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class LegendPane extends VisualizationTab implements Drawer{

    private Config config;
    private Pane pane;
    ScrollPane scrollPane;
    private Scene scene;
    private Canvas canvas;
    private Canvas labelCanvas;
    private  PacketPane packetPane;

    ScrollPane labelScrollPane;
    private double xpos;

    private double oldSceneX, oldTranslateX, xPos;
    private Pane labelPane;
    private static final double labelWidth = 135;
    private static final double fontSize = 8.5;


    public LegendPane(Scene scene, Config config) {
        this.scene = scene;
        this.config = config;
    }


    public void createScrollPane(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, int lines) {

        oldSceneX = oldTranslateX =  xPos = 0;

        pane = new Pane();
        pane.setMaxSize(scene.getWidth(),scene.getHeight());

        labelPane = new Pane();
        labelPane.setMaxSize(scene.getWidth(),scene.getHeight());

        double scrollPaneHeight = scene.getHeight()* legendScrollPaneHeightRatio;

        scrollPane = new ScrollPane(pane);
        scrollPane.setMaxSize(scene.getWidth(),scrollPaneHeight);
        scrollPane.setMinHeight(scrollPaneHeight);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        labelScrollPane = new ScrollPane(labelPane);
        labelScrollPane.setMaxSize(labelWidth, scrollPaneHeight);
        labelScrollPane.setMinHeight(scrollPaneHeight);
        labelScrollPane.setPannable(true);
        labelScrollPane.setFitToWidth(true);
        labelScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        labelScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        double canvasHeigth = lines * miniPacketImageSize;
        if(canvasHeigth < scrollPaneHeight) {
            canvasHeigth = scrollPaneHeight;
        }
        canvas = new Canvas(scene.getWidth(), canvasHeigth);
        labelCanvas = new Canvas(labelWidth,canvasHeigth);

        addListenersAndHandlers(stream, packets, sortedPIDs);
    }


    protected void drawPackets(Stream stream, ArrayList<TSpacket> packets, List sorted, double xPos) {

        GraphicsContext graphicsContextLegendCanvas = canvas.getGraphicsContext2D();

        graphicsContextLegendCanvas.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContextLegendCanvas.setFill(Color.WHITE);
        graphicsContextLegendCanvas.fillRect(0,0, canvas.getWidth(), canvas.getHeight());

        int index = 0;
        for (TSpacket packet : packets) {
            if(isInViewport(scene, index * miniPacketImageSize,(-1) * xPos)) {
                int pid = packet.getPID();
                drawMiniPacket(graphicsContextLegendCanvas, pid,  xPos + index * miniPacketImageSize, sorted.indexOf(pid));
            }
            index++;
        }
        // return legendCanvas;
    }


    private void drawMiniPacket(GraphicsContext graphicsContext, int type, double x, double y) {
        final int offset = 2;
        graphicsContext.setFill(config.getPacketColor(type));
        graphicsContext.fillRect(x + offset, y * miniPacketImageSize + offset , miniPacketImageSize-offset, miniPacketImageSize-offset); //x,y,height, width, archeigth, arcwidh
    }


    protected void createLabels(Map<Integer, Integer> PIDs) {

        int y = 9,x = 2, labelLength = 7;
        double gap = 2;

        GraphicsContext graphicsContextLabelCanvas = labelCanvas.getGraphicsContext2D();

        graphicsContextLabelCanvas.clearRect(0, 0, labelCanvas.getWidth(), labelCanvas.getHeight());
        graphicsContextLabelCanvas.setFill(Color.WHITE);
        graphicsContextLabelCanvas.fillRect(0,0, labelCanvas.getWidth(), labelCanvas.getHeight());

        for (Map.Entry<Integer, Integer> pid : PIDs.entrySet()) {

            graphicsContextLabelCanvas.setFont(new Font(fontSize));
            graphicsContextLabelCanvas.strokeText("PID: " + pid.getKey().toString(),x,y);
            y+=fontSize+gap;
        }
        labelPane.getChildren().add(labelCanvas);
    }


    public void addListenersAndHandlers(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs) {

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                    labelScrollPane.setVvalue(scrollPane.getVvalue());
                    packetPane.scrollPane.setVvalue(scrollPane.getVvalue());
                }
        );

        labelScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                    scrollPane.setVvalue(labelScrollPane.getVvalue());
                }
        );

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = scene.getWidth();

            canvas.setWidth(newWidth);
            scrollPane.setMaxWidth(newWidth);

            drawCanvas(stream, packets,sortedPIDs, xPos);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            drawCanvas(stream, packets,sortedPIDs, xPos);
        });

        pane.setOnMousePressed(mouseEvent -> {
            updateX(mouseEvent);
        });

        pane.setOnMouseDragged(mouseEvent -> {
            double translate = translate(mouseEvent.getSceneX());
            xPos += translate;
            if(xPos > 0) {
                xPos = 0;
            }
            drawCanvas(stream, packets,sortedPIDs, xPos);
            packetPane.setXpos(xPos*legendPaneMoveCoeff);
            packetPane.drawCanvas(stream, packets,sortedPIDs, xPos * legendPaneMoveCoeff);
            updateX(mouseEvent);
        });
    }


    void drawCanvas(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {

        drawPackets(stream, packets, sortedPIDs,xPos);

        pane.getChildren().clear();
        pane.getChildren().addAll(canvas);
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
        this.xpos = xpos;
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
}
