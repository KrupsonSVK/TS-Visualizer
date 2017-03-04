package view.visualizationTab;

import app.Config;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import model.Stream;
import model.TSpacket;

import java.util.ArrayList;
import java.util.List;


public class PacketPane extends VisualizationTab{


    private Tooltip tooltip;

    private Config config;
    private Pane packetPane;
    public ScrollPane scrollPane;
    private Scene scene;
    private Canvas packetCanvas;

    final static double packetImageWidth = 100;
    private final static double packetImageHeigth = 60;
    private final static double packetScrollPaneHeigthRatio = 0.54;

    private double oldSceneX, oldTranslateX, oldPacketSceneX, oldPacketTranslateX, xPos;


    public PacketPane(Scene scene, Config config) {
        tooltip = new Tooltip();
        this.scene = scene;
        this.config = config;
    }

    void createPacketScrollPane(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, int lines) {

        oldSceneX = oldTranslateX = oldPacketTranslateX = oldTranslateX = xPos = 0;

        packetPane = new Pane();
        packetPane.setMaxSize(scene.getWidth(),scene.getHeight());

        scrollPane = new ScrollPane(packetPane);
        scrollPane.setMaxSize(scene.getWidth(),scene.getHeight() * packetScrollPaneHeigthRatio);//54%
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);

        double canvasHeigth = lines * packetImageHeigth;
        if(canvasHeigth < scrollPane.getMaxHeight())
            canvasHeigth = scrollPane.getMaxHeight();

        packetCanvas = new Canvas(scene.getWidth(), canvasHeigth);

        packetPane.setOnMousePressed(mouseEvent -> {
            updateX(mouseEvent);
            if(tooltip.isShowing())
                tooltip.hide();
        });

        packetPane.setOnMouseDragged( mouseEvent -> {
            double translate = oldPacketTranslateX + mouseEvent.getSceneX() - oldPacketSceneX;
            xPos += translate;// / mouseSensitivity;
            if(xPos > 0)
                xPos = 0;

            drawPacketCanvas(stream, packets, sortedPIDs, xPos);
            drawLegendCanvas(stream, packets, sortedPIDs, xPos/3);
            updateX(mouseEvent);
        });


        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = scene.getWidth();
            packetCanvas.setWidth(newWidth);
            packetPane.setMaxWidth(newWidth);
            scrollPane.setMaxWidth(newWidth);

            drawPacketCanvas(stream, packets,sortedPIDs, xPos);
            //drawLegendCanvas(stream, packets,sortedPIDs, xPos);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
           // scrollPane.setMaxHeight(scene.getHeight() - legendScrollPane.getHeight() - barScrollPane.getHeight());
            drawPacketCanvas(stream, packets,sortedPIDs, xPos);
            //drawLegendCanvas(stream, packets,sortedPIDs, xPos);

        });
    }


    void drawPacketCanvas(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, double xPos) {

        drawPackets(stream, packets, sortedPIDs,xPos);

        packetPane.getChildren().clear();
        packetPane.getChildren().add(packetCanvas);
    }


    private void drawPackets(Stream stream, ArrayList<TSpacket> packets, List sorted, double xPos) {

        GraphicsContext graphicsContextPacketCanvas = packetCanvas.getGraphicsContext2D();

        graphicsContextPacketCanvas.clearRect(0, 0, packetCanvas.getWidth(), packetCanvas.getHeight());
        graphicsContextPacketCanvas.setFill(Color.WHITE);
        graphicsContextPacketCanvas.fillRect(0,0, packetCanvas.getWidth(), packetCanvas.getHeight());

        int index = 0;
        for (TSpacket packet : packets) {
            if(isInViewport(index * packetImageWidth,(-1) * xPos)) {
                int pid = packet.getPID();
                drawPacketImg(graphicsContextPacketCanvas, pid, pid, config.getProgramName(stream,pid), sorted.indexOf(pid), xPos + index * packetImageWidth);
            }
            index++;
        }
        //return packetCanvas;
    }


    private void drawPacketImg(GraphicsContext graphicsContext, int type, int pid, String name, int yPos, double xPos) {
        xPos -= 25;
        yPos *= packetImageHeigth;

        javafx.scene.image.Image original = new javafx.scene.image.Image(getClass().getResourceAsStream("/app/resources/" + config.getPacketImageName(type))); //TODO toto musia byt array of finals
        graphicsContext.drawImage(original, xPos, yPos, packetImageWidth, packetImageHeigth);
        graphicsContext.setFont(new Font(8));
        graphicsContext.strokeText("PID: " + pid + "\n" + config.getPacketName(pid) + "\n" + name, xPos + 5, yPos + 30);

        Rectangle rectangle = new Rectangle(xPos, yPos, packetImageWidth-10, packetImageHeigth);
        rectangle.setFill(Paint.valueOf("transparent"));

        rectangle.setOnMouseClicked(mouseEvent -> {
            Rectangle rect = (Rectangle) mouseEvent.getSource();
            tooltip.setText(getPacketInfo(rect));
            tooltip.show(rect, mouseEvent.getScreenX() + 50, mouseEvent.getScreenY());
        });

        packetPane.getChildren().add(rectangle);
    }


    void updateX(MouseEvent mouseEvent) {
        oldPacketSceneX = mouseEvent.getSceneX();
        oldPacketTranslateX = ((Pane) mouseEvent.getSource()).getTranslateX();
    }

    boolean isInViewport(double packetPosition, double start) {
        double end = start + scene.getWidth();
        return packetPosition >= start && packetPosition <= end;
    }

}
