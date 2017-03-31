package view.visualizationTab;

import model.Config;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import model.*;

import java.util.ArrayList;
import java.util.List;

import static model.Config.PSItype;


public class PacketPane extends VisualizationTab implements Drawer {

    private Tooltip_ tooltip;
    private Config config;

    private Scene scene;
    Pane pane;
    ScrollPane scrollPane;
    Canvas canvas;

    private LegendPane legendPane;
    private BarPane barPane;

    private ArrayList<Image> images;
    private ArrayList<TSpacket> packets;
    private List<Integer> sortedPIDs;
    private List<Rectangle> rectangles;

    private double oldSceneX, oldTranslateX, xPos;


    public PacketPane(Scene scene, Config config) {
        tooltip = new Tooltip_();
        this.scene = scene;
        this.config = config;
        rectangles = new ArrayList<>();
    }


    public void createScrollPane(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, int lines) {

        oldSceneX = oldTranslateX = xPos = 0;

        this.stream = stream;
        this.packets = packets;
        this.sortedPIDs = sortedPIDs;

        tooltip.setPackets(packets);
        tooltip.setConfig(config);

        pane = new Pane();
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        scrollPane = new ScrollPane(pane);
        scrollPane.setMaxSize(scene.getWidth(), scene.getHeight() * packetScrollPaneHeightRatio);//54%
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);

        double canvasHeigth = lines * packetImageHeight;
        if (canvasHeigth < scrollPane.getMaxHeight()) {
            canvasHeigth = scrollPane.getMaxHeight();
        }
        canvas = new Canvas(scene.getWidth(), canvasHeigth);

        addListenersAndHandlers(stream, packets, sortedPIDs);
    }


    protected void drawPackets(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {

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
                drawPacketImg(graphicsContextPacketCanvas, sortedPIDs.indexOf(pid), newPos, getType(packet), pid, config.getProgramName(stream, pid), isAdaptationField , isPayloadStart, packet.hashCode());
            }
            index++;
        }
    }


    private int getType(TSpacket packet) {
        if (config.isPSI(packet.getPID()))
            return PSItype;
        return config.getPEStype(stream.getPEScode(packet.getPID()));
    }


    private void drawPacketImg(GraphicsContext graphicsContext,  int yPos, double xPos, int type, int pid, String name, boolean isAdaptationField, boolean isPayloadStart, int packetHash) {
        double offset = 50;
        double xPadding = 8;
        double margin = 4;
        double shadowSize = 10;

        xPos -= packetImageHeight / 2;
        yPos *= packetImageHeight;

        Image packetImage = (Image) config.packetImages.get(pid);
        graphicsContext.drawImage(packetImage, xPos, yPos, packetImageWidth, packetImageHeight);

        Image typeIcon = (Image) config.typeIcons.get(type);
        graphicsContext.drawImage(typeIcon, xPos + 2*typeIconSize + xPadding , yPos + typeIconSize , typeIconSize, typeIconSize);

        if (isAdaptationField){
            Image icon = (Image) config.typeIcons.get(config.adaptationFieldIcon);
            graphicsContext.drawImage(icon, xPos + margin,  yPos + typeIconSize + margin +  typeIconSize,specialIconSize, specialIconSize);
        }
        if (isPayloadStart){
            Image icon = (Image) config.typeIcons.get(config.PESheaderIcon);
            graphicsContext.drawImage(icon, xPos + 2*typeIconSize + xPadding , yPos + 2*typeIconSize + margin/2 , specialIconSize, specialIconSize);
        }

        graphicsContext.setFont(new Font(fontSize));
        graphicsContext.strokeText("PID: " + pid + "\n" + config.getPacketName(pid) + "\n" + name, xPos + margin, yPos + offset*0.55);

        Rectangle rectangle = new Rectangle(xPos, yPos, packetImageWidth - shadowSize, packetImageHeight);
        rectangle.setFill(Paint.valueOf("transparent"));

        rectangle.setOnMouseClicked(mouseEvent -> {
                    tooltip.setText(tooltip.getPacketInfo(packetHash));  //TODO je uplne zle!!!
                    tooltip.setStyle("-fx-font-family: monospace");
                    tooltip.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX() + offset, mouseEvent.getScreenY());
                }
        );
        pane.getChildren().add(rectangle);
        rectangle.toFront();
    }


    public void addListenersAndHandlers(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs) {

        pane.setOnMousePressed(mouseEvent -> {
            updateX(mouseEvent);
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
            barPane.rectangle.setX(-xPos / legendPaneMoveCoeff / getLookingGlassMoveCoeff());

            updateX(mouseEvent);
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


    void drawCanvas(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {
        pane.getChildren().clear();

        drawPackets(stream, packets, sortedPIDs, xPos);
        pane.getChildren().add(canvas);
        canvas.toBack();
    }


    private boolean isInRange(int packetX, double packetY, double xPos, double yPos) {

        if (packetX > xPos - 5 && packetX < xPos + 5) {
            if (packetY > yPos - 3 && packetY < yPos + 3) {
                return true;
            }
        }
        return false;
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
