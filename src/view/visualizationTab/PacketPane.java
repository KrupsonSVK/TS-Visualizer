package view.visualizationTab;

import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import model.config.dvb;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import model.*;

import java.util.ArrayList;
import java.util.List;

import static model.config.dvb.*;
import static model.config.config.*;


public class PacketPane extends VisualizationTab implements Drawer {

    private PacketInfo tooltip;
    private Scene scene;
    Pane pane;
    ScrollPane scrollPane;
    Canvas canvas;
    private LegendPane legendPane;
    private BarPane barPane;
    private double oldSceneX, oldTranslateX, xPos, yPos, initYpos, oldTranslateY, initVvalue;


    public PacketPane(Scene scene) {
        tooltip = new PacketInfo();
        this.scene = scene;
    }


    public void createScrollPane(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, int lines) {

        initVvalue = initYpos = oldTranslateY = yPos = oldSceneX = oldTranslateX = xPos = 0;

        this.stream = stream;
        this.sortedPIDs = sortedPIDs;

        tooltip.setPackets(packets);

        pane = new Pane();
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        scrollPane = new ScrollPane(pane);
        scrollPane.setMaxSize(scene.getWidth(), scene.getHeight() * packetScrollPaneHeightRatio);//54%
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);

        double canvasHeigth = lines * packetImageHeight;
        if (canvasHeigth < scrollPane.getMaxHeight()) {
            canvasHeigth = scrollPane.getMaxHeight();
        }
        canvas = new Canvas(scene.getWidth(), canvasHeigth);

        addListenersAndHandlers(stream, packets, sortedPIDs);
    }

    @Override
    public void drawPackets(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {

        GraphicsContext graphicsContextPacketCanvas = canvas.getGraphicsContext2D();

        graphicsContextPacketCanvas.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContextPacketCanvas.setFill(Color.WHITE);
        graphicsContextPacketCanvas.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int index = 0;
        for (TSpacket packet : packets) {
            if (isInViewport(scene, index * packetImageWidth, -xPos)) {
                int pid = packet.getPID();
                double newPos = xPos + index * packetImageWidth;
                boolean isPayloadStart = packet.getPayload()!=null ? packet.getPayload().hasPESheader() : false;
                boolean isAdaptationField = packet.getAdaptationFieldControl() > 1; //packet.getAdaptationFieldHeader() != null;
                drawPacketImg(graphicsContextPacketCanvas, sortedPIDs.indexOf(pid), newPos, dvb.getType(packet,stream), pid, dvb.getProgramName(stream, pid), isAdaptationField , isPayloadStart);
                pane.getChildren().add(createListenerRect( sortedPIDs.indexOf(pid), newPos, packet.hashCode()));
            }
            index++;
        }
    }

    private Rectangle createListenerRect(int yPos, double xPos, int packetHash){
        double shadowSize = 10;

        xPos -= packetImageHeight / 2;
        yPos *= packetImageHeight;

        Rectangle rectangle = new Rectangle(xPos, yPos, packetImageWidth - shadowSize, packetImageHeight);
        rectangle.setFill(Paint.valueOf("transparent"));

        rectangle.setOnMouseClicked(mouseEvent -> {
                    tooltip.setText(tooltip.getPacketInfo(packetHash));
                    tooltip.setStyle("-fx-font-family: monospace");
                    tooltip.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
        );
        pane.toBack();
        rectangle.toFront();
        return rectangle;
    }


    private void drawPacketImg(GraphicsContext graphicsContext,  int yPos, double xPos, int type, int pid, String name, boolean isAdaptationField, boolean isPayloadStart) {
        double offset = 50;
        double xPadding = 8;
        double margin = 4;

        xPos -= packetImageHeight / 2;
        yPos *= packetImageHeight;

        Image packetImage = (Image) packetImages.get(pid);
        graphicsContext.drawImage(packetImage, xPos, yPos, packetImageWidth, packetImageHeight);

        if(pid == nullPacket){
            Image typeIcon = (Image) typeIcons.get(nullPacket);
            graphicsContext.drawImage(typeIcon, xPos + 2*typeIconSize + xPadding , yPos + typeIconSize , typeIconSize, typeIconSize);
        }
        else {
            Image typeIcon = (Image) typeIcons.get(type);
            graphicsContext.drawImage(typeIcon, xPos + 2 * typeIconSize + xPadding, yPos + typeIconSize, typeIconSize, typeIconSize);
        }
        if (isAdaptationField){
            Image icon = (Image) typeIcons.get(dvb.adaptationFieldIcon);
            graphicsContext.drawImage(icon, xPos + margin,  yPos + typeIconSize + margin +  typeIconSize,specialIconSize, specialIconSize);
        }
        if (isPayloadStart){
            Image icon = (Image) typeIcons.get(dvb.PESheaderIcon);
            graphicsContext.drawImage(icon, xPos + 2*typeIconSize + xPadding , yPos + 2*typeIconSize + margin/2 , specialIconSize, specialIconSize);
        }

        graphicsContext.setFont(new Font(fontSize));
        graphicsContext.strokeText("PID: " + pid + "\n" + dvb.getPacketName(pid) + "\n" + name, xPos + margin, yPos + offset*0.55);
    }


    public void addListenersAndHandlers(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs) {

        pane.setOnMousePressed(mouseEvent -> {
            updateX(mouseEvent);
            updateY(mouseEvent);
        });

        pane.setOnMouseReleased(mouseEvent -> {
            tooltip.hideTooltip();
        });

        pane.setOnMouseDragged(mouseEvent -> {
            tooltip.hideTooltip();

            xPos += translate(mouseEvent.getSceneX());
            xPos = stayInRange(xPos);

            drawCanvas(stream, packets, sortedPIDs, xPos);

            legendPane.setXpos(xPos / legendPaneMoveCoeff);
            legendPane.drawCanvas(stream, packets, sortedPIDs, xPos / legendPaneMoveCoeff);

            barPane.setXpos(-xPos / legendPaneMoveCoeff / getLookingGlassMoveCoeff());
            barPane.lookingGlass.setX(-xPos / legendPaneMoveCoeff / getLookingGlassMoveCoeff());

            updateX(mouseEvent);

            double hvalue = initVvalue - ( translateY(mouseEvent) / getMoveCoeff() );
            scrollPane.setVvalue(hvalue);
        });

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = scene.getWidth();

            canvas.setWidth(newWidth);
            scrollPane.setMaxWidth(newWidth);

            drawCanvas(stream, packets, sortedPIDs, xPos);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            double newHeigth = scene.getHeight() - legendScrollPaneHeight - barScrollPaneHeight;
            scrollPane.setMaxHeight(newHeigth);

            drawCanvas(stream, packets, sortedPIDs, xPos);
        });

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                    legendPane.labelScrollPane.setVvalue(scrollPane.getVvalue());
                    legendPane.scrollPane.setVvalue(scrollPane.getVvalue());
                }
        );
    }

    private void updateY(MouseEvent mouseEvent) {
        initVvalue = scrollPane.getVvalue();
        initYpos = mouseEvent.getSceneY();
        oldTranslateY = ((Pane) mouseEvent.getSource()).getTranslateY();
    }


    private double translateY(MouseEvent mouseEvent) {
        return ((oldTranslateY + mouseEvent.getSceneY() - initYpos) / scrollPane.getHeight());
    }


    private double getMoveCoeff() {
        return mouseSensitivityVertical * ( Screen.getPrimary().getVisualBounds().getMaxY() / scrollPane.getHeight() );
    }


    @Override
    public void drawCanvas(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {
        pane.getChildren().clear();

        drawPackets(stream, packets, sortedPIDs, xPos);
        pane.getChildren().add(canvas);
        canvas.toBack();
    }

    @Override
    public double getLookingGlassMoveCoeff() {
        return miniPacketImageSize / scene.getWidth() * stream.getPackets().size();
    }

    @Override
    public void updateX(MouseEvent mouseEvent) {
        oldSceneX = mouseEvent.getSceneX();
        oldTranslateX = ((Node) mouseEvent.getSource()).getTranslateX();
    }

    @Override
    public double stayInRange(double xPos) {
        if (xPos > 0) {
            return 0;
        }
        return xPos;
    }

    @Override
    public double translate(double sceneX) {
        return oldTranslateX + sceneX - oldSceneX;
    }

    @Override
    public void setXpos(double xPos) {
        this.xPos = xPos;
    }

    public void setLegendPane(LegendPane legendPane) {
        this.legendPane = legendPane;
    }

    public void setBarPane(BarPane barPane) {
        this.barPane = barPane;
    }
}
