package view.visualizationTab;

import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import model.config.DVB;
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
import model.packet.Packet;
import model.pes.PES;

import java.util.ArrayList;
import java.util.Map;

import static model.config.DVB.*;
import static model.config.Config.*;


public class PacketPane extends VisualizationTab implements Drawer {

    private PacketInfo tooltip;
    private Scene scene;
    Pane pane;
    ScrollPane scrollPane;
    Canvas canvas;
    private LegendPane legendPane;
    private BarPane barPane;
    private double oldSceneX, oldTranslateX, xPos, yPos, initYpos, oldTranslateY, initVvalue;
    private double xpos;


    public PacketPane(Scene scene) {
        tooltip = new PacketInfo();
        this.scene = scene;
    }


    public void createScrollPane(Stream stream, ArrayList<Packet> packets, Map sortedPIDs, int lines) {

        initVvalue = initYpos = oldTranslateY = yPos = oldSceneX = oldTranslateX = xPos = 0;

        this.stream = stream;
        this.sortedPIDs = sortedPIDs;

        tooltip.setPackets(packets);
        tooltip.setStream(stream);

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

        addListenersAndHandlers(stream, packets);
    }

    @Override
    public void drawPackets(Stream stream, ArrayList<Packet> packets, double xPos) {

        GraphicsContext graphicsContextPacketCanvas = canvas.getGraphicsContext2D();

        graphicsContextPacketCanvas.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContextPacketCanvas.setFill(Color.WHITE);
        graphicsContextPacketCanvas.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int index = 0;
        for (Packet packet : packets) {
            int pid = packet.getPID();
            Integer yPos = (Integer) sortedPIDs.get(pid);
            if(yPos != null) {
                if (isInViewport(scene, index * packetImageWidth, -xPos)) {
                    double newPos = xPos + index * packetImageWidth;
                    boolean hasPESheader = packet.getPayload() != null ? packet.getPayload().hasPESheader() : false;
                    boolean isPayloadStart = packet.getPayloadStartIndicator() == 1;
                    boolean isPMT = isPMT(stream.getTables().getPATmap(),packet.getPID());
                    boolean isAdaptationField = packet.getAdaptationFieldControl() > 1; //packet.getAdaptationFieldHeader() != null;
                    drawPacketImg(graphicsContextPacketCanvas, yPos, newPos, getType(packet.getPID(), stream), pid, DVB.getProgramName(stream, pid), isAdaptationField, isPayloadStart, isPMT,hasPESheader,hasTimestamp(packet) );
                    pane.getChildren().add(createListenerRect(yPos, newPos, packet.hashCode()));
                }
            }
            index++;
        }
    }


    private boolean hasTimestamp(Packet packet) {
        if(packet.getAdaptationFieldHeader()!=null) {
            if (packet.getAdaptationFieldHeader().getPCRF() == 0x01 || packet.getAdaptationFieldHeader().getOPCRF() == 0x01) {
                return true;
            }
        }
        if(packet.getPayload()!=null) {
            if (((PES) packet.getPayload()).getPTSdtsFlags() >= 1) {
                return true;
            }
        }
        return false;
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


    private void drawPacketImg(GraphicsContext graphicsContext,  int yPos, double xPos, int type, int pid, String name, boolean isAdaptationField, boolean isPayloadStart, boolean isPMT, boolean hasPESheader, boolean hasTimestamp) {
        double offset = 50;
        double xPadding = 8;
        double margin = specialIconSize / 4;
        double xMargin = specialIconSize / 4;

        xPos -= packetImageHeight / 2;
        yPos *= packetImageHeight;

        Image packetImage = (Image) packetImages.get(pid);
        graphicsContext.drawImage(packetImage, xPos, yPos, packetImageWidth, packetImageHeight);

        Image typeIcon = (Image) typeIcons.get(type);
        graphicsContext.drawImage(typeIcon, xPos + 2 * typeIconSize + xPadding + 1, yPos + typeIconSize, typeIconSize, typeIconSize);
        {
            double y = yPos + typeIconSize + margin + typeIconSize;
            if (isAdaptationField) {
                Image icon = (Image) typeIcons.get(adaptationFieldIcon);
                graphicsContext.drawImage(icon, xPos + xMargin, y, specialIconSize, specialIconSize);
                xMargin += typeIconSize;
            }
            if (isPMT) {
                Image icon = (Image) typeIcons.get(PMTicon);
                graphicsContext.drawImage(icon, xPos + xMargin, y, specialIconSize, specialIconSize);
                xMargin += typeIconSize;
            }
            if (isPayloadStart) {
                Image icon = (Image) typeIcons.get(payloadStartIcon);
                graphicsContext.drawImage(icon, xPos + xMargin, y, specialIconSize, specialIconSize);
                xMargin += typeIconSize;
            }
            if (hasPESheader) {
                Image icon = (Image) typeIcons.get(PESheaderIcon);
                graphicsContext.drawImage(icon, xPos + xMargin, y, specialIconSize, specialIconSize);
                xMargin += typeIconSize;
            }
            if(hasTimestamp){
                Image icon = (Image) typeIcons.get(timestampIcon);
                graphicsContext.drawImage(icon, xPos + xMargin, y, specialIconSize, specialIconSize);
            }
        }
        graphicsContext.setFont(new Font(fontSize));
        graphicsContext.strokeText("PID: " + pid + "\n" + DVB.getPacketName(pid) + "\n" + name, xPos + margin, yPos + offset*0.55);
    }


    public void addListenersAndHandlers(Stream stream, ArrayList<Packet> packets) {

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

            drawCanvas(stream, packets, xPos);

            legendPane.setXpos(xPos / legendPaneMoveCoeff);
            legendPane.drawCanvas(stream, packets, xPos / legendPaneMoveCoeff);

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

            drawCanvas(stream, packets,  xPos);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            double newHeigth = scene.getHeight() - legendScrollPaneHeight - barScrollPaneHeight;
            scrollPane.setMaxHeight(newHeigth);

            drawCanvas(stream, packets,  xPos);
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
    public void drawCanvas(Stream stream, ArrayList<Packet> packets, double xPos) {
        pane.getChildren().clear();

        drawPackets(stream, packets, xPos);
        pane.getChildren().add(canvas);
        canvas.toBack();
    }

    @Override
    public double getLookingGlassMoveCoeff() {
        return miniPacketImageSize / scene.getWidth() * stream.getTables().getPackets().size();
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

    public void setSortedPIDs(Map sortedPIDs) {
        this.sortedPIDs = sortedPIDs;
    }

    public double getXpos() {
        return xPos;
    }
}
