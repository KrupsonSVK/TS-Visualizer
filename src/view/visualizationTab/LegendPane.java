package view.visualizationTab;

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

import static model.config.Config.*;


public class LegendPane extends VisualizationTab implements Drawer{

    private Scene scene;
    private Pane pane;
    ScrollPane scrollPane;
    private Canvas canvas;
    private Canvas labelCanvas;
    private Pane labelPane;
    ScrollPane labelScrollPane;
    private PacketPane packetPane;
    private BarPane barPane;
    private double oldSceneX;
    private double oldTranslateX;
    private double xPos;


    public LegendPane(Scene scene) {
        this.scene = scene;
    }


    public void createScrollPane(Stream stream, ArrayList<TSpacket> packets, Map sortedPIDs, int lines) {

        oldSceneX = oldTranslateX = xPos = 0;
        this.stream = stream;
        this.sortedPIDs = sortedPIDs;

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

        addListenersAndHandlers(stream, packets);
    }

    @Override
    public void drawPackets(Stream stream, ArrayList<TSpacket> packets, double xPos) {

        GraphicsContext graphicsContextLegendCanvas = canvas.getGraphicsContext2D();

        graphicsContextLegendCanvas.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        graphicsContextLegendCanvas.setFill(defaultColor);
        graphicsContextLegendCanvas.fillRect(0,0, canvas.getWidth(), canvas.getHeight());

        double yPos = packetPane.scrollPane.getVvalue()*(canvas.getHeight()-getLegendScopeHeigth());
        graphicsContextLegendCanvas.setFill(Color.WHITE);
        graphicsContextLegendCanvas.fillRect(0,yPos, getLegendScopeWidth(), getLegendScopeHeigth());

        int index = 0;
        for (TSpacket packet : packets) {
            if(isInViewport(scene, index * miniPacketImageSize,-xPos)) {
                int pid = packet.getPID();
                boolean isPayloadStart = packet.getPayload() != null ? packet.getPayload().hasPESheader() : false;
                boolean isAdaptationField = packet.getAdaptationFieldHeader() != null;
                drawMiniPacket(graphicsContextLegendCanvas, pid,  xPos + index * miniPacketImageSize, ((Integer) sortedPIDs.get(pid)).doubleValue(),isAdaptationField,isPayloadStart);
            }
            index++;
        }
    }


    private double getLegendScopeHeigth() {
        return ( scene.getHeight() / packetImageHeight * miniPacketImageSize );
    }


    private double getLegendScopeWidth() {
        return ( scene.getWidth() / packetImageWidth * miniPacketImageSize );
    }


    private void drawMiniPacket(GraphicsContext graphicsContext, int type, double x, double y, boolean isAdaptationField, boolean isPayloadStart) {

        if (isAdaptationField && isPayloadStart){
            graphicsContext.setFill(payloadStartColor);
            graphicsContext.fillRect(x + offsetLP, y * miniPacketImageSize + offsetLP, miniPacketImageSize- offsetLP, miniPacketImageSize- offsetLP);
            graphicsContext.setFill(adaptationFieldColor);
            graphicsContext.fillRect(x + posOffset(secondaryFrameSize), y * miniPacketImageSize + posOffset(secondaryFrameSize) , miniPacketImageSize -  sizeOffset(secondaryFrameSize), miniPacketImageSize - sizeOffset(secondaryFrameSize));//x,y,height, width, archeigth, arcwidh
            graphicsContext.setFill(getPacketColor(type));
            graphicsContext.fillRect(x + posOffset(primaryFrameSize), y * miniPacketImageSize + posOffset(primaryFrameSize), miniPacketImageSize - sizeOffset(primaryFrameSize), miniPacketImageSize - sizeOffset(primaryFrameSize));
        }
        else if (isAdaptationField){
            graphicsContext.setFill(adaptationFieldColor);
            graphicsContext.fillRect(x + offsetLP, y * miniPacketImageSize + offsetLP, miniPacketImageSize- offsetLP, miniPacketImageSize- offsetLP);
            graphicsContext.setFill(getPacketColor(type));
            graphicsContext.fillRect(x + posOffset(primaryFrameSize), y * miniPacketImageSize + posOffset(primaryFrameSize), miniPacketImageSize - sizeOffset(primaryFrameSize), miniPacketImageSize - sizeOffset(primaryFrameSize));

        }
        else if (isPayloadStart){
            graphicsContext.setFill(payloadStartColor);
            graphicsContext.fillRect(x + offsetLP, y * miniPacketImageSize + offsetLP, miniPacketImageSize- offsetLP, miniPacketImageSize- offsetLP);
            graphicsContext.setFill(getPacketColor(type));
            graphicsContext.fillRect(x + posOffset(primaryFrameSize), y * miniPacketImageSize + posOffset(primaryFrameSize), miniPacketImageSize - sizeOffset(primaryFrameSize), miniPacketImageSize - sizeOffset(primaryFrameSize));
        }
        else {
            graphicsContext.setFill(getPacketColor(type));
            graphicsContext.fillRect(x + offsetLP, y * miniPacketImageSize + offsetLP, miniPacketImageSize - offsetLP, miniPacketImageSize - offsetLP);
        }
    }

    private double sizeOffset(double i) {
        return i* offsetLP;
    }

    private double posOffset(double i) {
        return (sizeOffset(i) * 0.75);
    }


    protected void createLabels(Map<Integer, Integer> PIDs) {

        int y = 9;
        int x = 2;
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
        labelPane.getChildren().clear();
        labelPane.getChildren().add(labelCanvas);
    }


    public void addListenersAndHandlers(Stream stream, ArrayList<TSpacket> packets) {

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                    labelScrollPane.setVvalue(scrollPane.getVvalue());
                    packetPane.scrollPane.setVvalue(scrollPane.getVvalue());
                    drawCanvas(stream,packets,xPos);
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

            drawCanvas(stream,packets, xPos);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            drawCanvas(stream,packets, xPos);
        });

        pane.setOnMousePressed(mouseEvent -> {
            updateX(mouseEvent);
        });

        pane.setOnMouseDragged(mouseEvent -> {

            xPos += translate(mouseEvent.getSceneX());
            xPos = stayInRange(xPos);

            drawCanvas(stream, packets, xPos);

            packetPane.setXpos(xPos*legendPaneMoveCoeff);
            packetPane.drawCanvas(stream, packets, xPos * legendPaneMoveCoeff);

            barPane.setXpos(-xPos / getLookingGlassMoveCoeff());
            barPane.lookingGlass.setX(-xPos / getLookingGlassMoveCoeff());

            updateX(mouseEvent);
        });
    }

    @Override
    public void drawCanvas(Stream stream, ArrayList<TSpacket> packets, double xPos) {

        drawPackets(stream,  packets, xPos);

        pane.getChildren().clear();
        pane.getChildren().addAll(canvas);
    }

    @Override
    public double getLookingGlassMoveCoeff() {
        return miniPacketImageSize / scene.getWidth() * stream.getPackets().size() ;
    }

    @Override
    public double stayInRange(double xPos) {
        if (xPos > 0) {
            return 0;
        }
        return xPos;
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
    public void setXpos(double xPos) {
        this.xPos = xPos;
    }

    public void setPacketPane(PacketPane packetPane) {
        this.packetPane = packetPane;
    }

    public void setBarPane(BarPane barPane) {
        this.barPane = barPane;
    }

    public void setSortedPIDs(Map sortedPIDs) {
        this.sortedPIDs = sortedPIDs;
    }
}
