package view.visualizationTab;

import app.Config;
import model.Stream;
import model.TSpacket;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.List;


public class PacketPane extends VisualizationTab implements Drawer{

    private Tooltip tooltip;
    private Config config;
    Pane pane;
    ScrollPane scrollPane;
    private Scene scene;
    Canvas canvas;
    private ArrayList<Image> images;
    private LegendPane legendPane;

    private double oldSceneX, oldTranslateX, xPos;
    private ArrayList<TSpacket> packets;
    private List<Integer> sortedPIDs;
    private List<Rectangle> rectangles;


    public PacketPane(Scene scene, Config config) {
        tooltip = new Tooltip();
        this.scene = scene;
        this.config = config;
        rectangles = new ArrayList<>();
    }


    public void createScrollPane(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, int lines) {

        oldSceneX = oldTranslateX =  xPos = 0;

        this.packets = packets;
        this.sortedPIDs = sortedPIDs;

        pane = new Pane();
        pane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);

        scrollPane = new ScrollPane(pane);
        scrollPane.setMaxSize(scene.getWidth(),scene.getHeight() * packetScrollPaneHeightRatio);//54%
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);

        double canvasHeigth = lines * packetImageHeight;
        if(canvasHeigth < scrollPane.getMaxHeight())
            canvasHeigth = scrollPane.getMaxHeight();

        canvas = new Canvas(scene.getWidth(), canvasHeigth);

        addListenersAndHandlers(stream, packets,sortedPIDs);
    }


    protected void drawPackets(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {

        GraphicsContext graphicsContextPacketCanvas = canvas.getGraphicsContext2D();

        graphicsContextPacketCanvas.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContextPacketCanvas.setFill(Color.WHITE);
        graphicsContextPacketCanvas.fillRect(0,0, canvas.getWidth(), canvas.getHeight());

        int index = 0;
        for (TSpacket packet : packets) {
            if(isInViewport(scene, index * packetImageWidth, -xPos)) {
                int pid = packet.getPID();
                double newPos = xPos + index * packetImageWidth;
                drawPacketImg(graphicsContextPacketCanvas, pid, pid, config.getProgramName(stream,pid), sortedPIDs.indexOf(pid), newPos);
            }
            index++;
        }
    }


    private void drawPacketImg(GraphicsContext graphicsContext, int type, int pid, String name, int yPos, double xPos) {
        double offset = 50;

        xPos -= packetImageHeight / 2;
        yPos *= packetImageHeight;

        javafx.scene.image.Image original = new javafx.scene.image.Image(getClass().getResourceAsStream("/app/resources/" + config.getPacketImageName(type))); //TODO toto musia byt array of finals
        graphicsContext.drawImage(original, xPos, yPos, packetImageWidth, packetImageHeight);
        graphicsContext.setFont(new Font(fontSize));
        graphicsContext.strokeText("PID: " + pid + "\n" + config.getPacketName(pid) + "\n" + name, xPos + 5, yPos + 30);

        Rectangle rectangle = new Rectangle(xPos, yPos, packetImageWidth-10, packetImageHeight);
        rectangle.setFill(Paint.valueOf("transparent"));

        rectangle.setOnMouseClicked(mouseEvent -> {
            hideTooltip();
            tooltip.setText(getPacketInfo(pid));
            tooltip.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX() + offset, mouseEvent.getScreenY());}
        );
        pane.getChildren().add(rectangle);
        rectangle.toFront();
    }

    private void hideTooltip() {
        if(tooltip.isShowing()) {
            tooltip.hide();
        }
    }


    public void addListenersAndHandlers(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs) {

        pane.setOnMousePressed(mouseEvent -> {
            updateX(mouseEvent);
        });

        pane.setOnMouseReleased(mouseEvent -> {
            hideTooltip();
        });

        pane.setOnMouseDragged( mouseEvent -> {
            hideTooltip();
            double translate = translate(mouseEvent.getSceneX());
            xPos += translate;
            if(xPos > 0)
                xPos = 0;

            drawCanvas(stream, packets, sortedPIDs, xPos);
            legendPane.setXpos(xPos/legendPaneMoveCoeff);
            legendPane.drawCanvas(stream, packets, sortedPIDs, xPos/legendPaneMoveCoeff);

            updateX(mouseEvent);
        });

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = scene.getWidth();

            canvas.setWidth(newWidth);
            scrollPane.setMaxWidth(newWidth);

            drawCanvas(stream, packets,sortedPIDs, xPos);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            double newHeigth = scene.getHeight() - legendScrollPaneHeight - barScrollPaneHeight;

            scrollPane.setMaxHeight(newHeigth);

            drawCanvas(stream, packets,sortedPIDs, xPos);
        });


    }


    void drawCanvas(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {
        pane.getChildren().clear();

        drawPackets(stream, packets, sortedPIDs,xPos);
        pane.getChildren().add(canvas);
        canvas.toBack();
    }


    String getPacketInfo(int PID) {

        for(TSpacket packet : packets) {
            if (packet.getPID() == PID) {
                return createPacketInfo(packet);
            }
        }
        return "Unable to collect packet data!";
    }


    private String createPacketInfo(TSpacket packet) {
        String adaptationField = null;
//        if (packet.getAdaptationFieldControl() > 0)
//            adaptationField = "Adaptation Field Length: " + packet.getAdaptationFieldHeader().getAdaptationFieldLength();

        return ("Packet PID: " + packet.getPID() + "\n" +
                "Transport Error Indicator: " + packet.getTransportErrorIndicator() + "\n" +
                "Payload Start Indicator: " + packet.getPayloadStartIndicator() + "\n" +
                "Transport Scrambling Control:" + packet.getTransportScramblingControl() + "\n" +
                "Continuity Counter: " + packet.getContinuityCounter() + "\n" +
                "Adaptation Field Control: " + packet.getAdaptationFieldControl() + "\n" +
                adaptationField + "\n" +
                "Payload: " + packet.getPayload() + "\n"
        );
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

    @Override
    public void setOldTranslateX(double oldTranslateX) {
        this.oldTranslateX= oldTranslateX;
    }

    @Override
    public void setOldSceneX(double oldSceneX) {
        this.oldSceneX = oldSceneX;
    }


    public void setLegendPane(LegendPane legendPane) {
        this.legendPane = legendPane;
    }


    public void setBarPane(BarPane barPane) {
        this.barPane = barPane;
    }
}
