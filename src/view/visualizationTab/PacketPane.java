package view.visualizationTab;

import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import model.config.MPEG;
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
import model.psi.PMT;

import java.util.ArrayList;
import java.util.Map;

import static model.config.MPEG.*;
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

    /**
     * Metóda vykresľuje pakety viditeľné na vykresľovacom plátne vizualizačnej karty
     *
     * @param stream  objekt triedy Stream so všetkými analyzovanými údajmi transportného toku
     * @param packets pole objektov triedy Packet, na základe ktorých sú vykresľované pakety
     * @param xPos    pozícia na x-ovej osi vykreľovacieho plátna
     */
    @Override
    public void drawPackets(Stream stream, ArrayList<Packet> packets, double xPos) {
        //získanie inštanie GraphicsContext vykresľovacieho plátna potrebnej na manipuláciu s jeho grafickým obsahom
        GraphicsContext graphicsContextPacketCanvas = canvas.getGraphicsContext2D();
        {
            graphicsContextPacketCanvas.clearRect(0,0,canvas.getWidth(),canvas.getHeight()); //vyčistenie plátna
            graphicsContextPacketCanvas.setFill(Color.WHITE);
            graphicsContextPacketCanvas.fillRect(0,0,canvas.getWidth(),canvas.getHeight()); //vybielenie plátna
        }
        int index = 0; //počítadlo ako koeficient posuvu paketu na x-ovej osy
        for (Packet packet : packets) { //cyklus prechádzania poľa všetkých paketov
            int PID = packet.getPID(); //PID aktuálneho paketu
            Integer yPos = (Integer) sortedPIDs.get(PID); //pozícia paketu na y-ovej osy na základe zoradeného zoznamu PIDov
            if(yPos != null) { //ak sa podarilo získať y-ovú pozíciu
                //ak sa paket nachádza na viditeľnej časti vykreslovacieho plátna
                if (isInViewport(scene, index * packetImageWidth, -xPos)) {
                    double newPos = xPos + index * packetImageWidth; //x-ová pozícia na základe absolútnej pozície paketu v transprtnom toku
                    //na základe hodnôt nasledujúcich premenných sú vykresľované v pakete dané ikony
                    boolean hasPESheader = packet.getPayload() != null ? packet.getPayload().hasPESheader() : false; //paket obsahuje PES hlavičku
                    boolean isPayloadStart = packet.getPayloadStartIndicator() == 1; //paket obsahuje začiatok užitočných dát PES paketu
                    boolean isPMT = isPMT(stream.getTables().getPATmap(),packet.getPID()); //paket nesie PMT tabuľku
                    boolean isAdaptationField = packet.getAdaptationFieldControl() > 1; //paket obsahuje adaptačné pole
                    //zavolanie metódy vykreslenia jedného paketu
                    drawPacketImg(
                            graphicsContextPacketCanvas,
                            yPos, //y-ová pozícia
                            newPos,  // x-ová pozícia
                            getType(packet.getPID(), stream),  //typ paketu
                            PID,
                            MPEG.getProgramName(stream, PID),  //názov programu resp. služby
                            isAdaptationField,
                            isPayloadStart,
                            isPMT,
                            hasPESheader,
                            hasTimestamp(packet) //prítomnosť synchronizačnej časovej značky v pakete
                    );
                    //pridanie imaginárnej plochy nad paketom, spravujúcej používateľovu interakciu, t.k. klikanie myšou na paket
                    pane.getChildren().add(createListenerRect(yPos, newPos, packet.hashCode()));
                }
            }
            index++; //incrementácia posuvu po x-ovej osy
        }
    }


    private boolean hasTimestamp(Packet packet) {
        if(packet.getAdaptationFieldHeader()!=null) {
            if (packet.getAdaptationFieldHeader().getPCRF() == 0x01 || packet.getAdaptationFieldHeader().getOPCRF() == 0x01) {
                return true;
            }
        }
        if(packet.getPayload()!=null) {
            if(packet.getPayload() instanceof PES) {
                if (((PES) packet.getPayload()).getPTSdtsFlags() >= 1) {
                    return true;
                }
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
        graphicsContext.strokeText("PID: " + pid + "\n" + MPEG.getPacketName(pid) + "\n" + name, xPos + margin, yPos + offset*0.55); //TODO change PES to PMT if so
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
