package view.visualizationTab;

import app.Config;
import model.*;

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
import model.pes.PES;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static app.Config.PSItype;


public class PacketPane extends VisualizationTab implements Drawer {

    private Tooltip tooltip;
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
        tooltip = new Tooltip();
        this.scene = scene;
        this.config = config;
        rectangles = new ArrayList<>();
    }


    public void createScrollPane(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, int lines) {

        oldSceneX = oldTranslateX = xPos = 0;

        this.stream = stream;
        this.packets = packets;
        this.sortedPIDs = sortedPIDs;

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
                drawPacketImg(graphicsContextPacketCanvas, getType(packet), pid, config.getProgramName(stream, pid), sortedPIDs.indexOf(pid), newPos);
            }
            index++;
        }
    }


    private int getType(TSpacket packet) {
        if (config.isPSI(packet.getPID()))
            return PSItype;
        return stream.getPEScode(packet.getPID());
    }


    private void drawPacketImg(GraphicsContext graphicsContext, int type, int pid, String name, int yPos, double xPos) {
        double offset = 50;
        double padding = 5;

        xPos -= packetImageHeight / 2;
        yPos *= packetImageHeight;

        Image packetImage = (Image) config.packetImages.get(pid);
        graphicsContext.drawImage(packetImage, xPos, yPos, packetImageWidth, packetImageHeight);

        Image typeIcon = (Image) config.typeIcons.get(type);
        graphicsContext.drawImage(typeIcon, xPos + 2 * typeIconSize + padding, yPos + typeIconSize + padding, typeIconSize, typeIconSize);

        graphicsContext.setFont(new Font(fontSize));
        graphicsContext.strokeText("PID: " + pid + "\n" + config.getPacketName(pid) + "\n" + name, xPos + 5, yPos + 30);

        Rectangle rectangle = new Rectangle(xPos, yPos, packetImageWidth - 10, packetImageHeight);
        rectangle.setFill(Paint.valueOf("transparent"));

        rectangle.setOnMouseClicked(mouseEvent -> {
                    tooltip.setText(getPacketInfo(pid));
                    tooltip.setStyle("-fx-font-family: monospace");
                    tooltip.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX() + offset, mouseEvent.getScreenY());
                }
        );
        pane.getChildren().add(rectangle);
        rectangle.toFront();
    }


    private void hideTooltip() {
        if (tooltip.isShowing()) {
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

        pane.setOnMouseDragged(mouseEvent -> {
            hideTooltip();

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


    String getPacketInfo(int PID) {

        for (TSpacket packet : packets) {
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

        return "Header: \n\n" +
                "Packet PID: " + String.format("0x%04X", packet.getPID() & 0xFFFFF) + " (" + packet.getPID() + ")\n" +
                "Transport Error Indicator: " + packet.getTransportErrorIndicator() + (packet.getTransportErrorIndicator() == 0 ? " (No error)" : " (Error packet)") + "\n" +
                "Payload Start Indicator: " + packet.getPayloadStartIndicator() + (packet.getPayloadStartIndicator() == 0 ? " (Normal payload)" : " (Payload start)") + "\n" +
                "Transport priority: " + packet.getTransportPriority() + (packet.getTransportPriority() == 0 ? " (Normal priority)" : " (High priority)") + "\n" +
                "Transport Scrambling Control: " + packet.getTransportScramblingControl() + (packet.getTransportScramblingControl() == 0 ? " (Not scrambled)" : " (Scrambled)") + "\n" +
                "Continuity Counter: " + String.format("0x%01X", packet.getContinuityCounter() & 0xFFFFF) + " (" + packet.getContinuityCounter() + ")" + "\n" +
                "Adaptation Field Control: " + packet.getAdaptationFieldControl() + (packet.getAdaptationFieldControl() == 1 ?
                " (Payload only)" : (packet.getAdaptationFieldControl() == 2 ?
                " (Adaptation field only)" : " (Adaptation field followed by payload)")) + "\n\n\n" +
                createAdaptationFieldHeaderOutput(packet.getAdaptationFieldHeader()) +
                createPESheaderOutput(packet.getPayload()) +
                createDataOutput(packet.getData()) + "\n";
    }


    private String  createPESheaderOutput(Payload payload) {
        if(payload != null) {
            if (payload.hasPESheader()) {
                PES pesPacket = (PES) payload;
                return (
                        "PES header: \n\n" +
                                "Stream ID: " + pesPacket.getStreamID() + " (" + config.getStreamDescription(pesPacket.getStreamID()) + ")\n" +
                                "PES packet length: " + pesPacket.getPESpacketLength() + "\n" +
                                "PES scrambling control: " + pesPacket.getPEScrcFlag() + "\n" +
                                "PES priority: " + pesPacket.getPESpriority() + "\n" +
                                "Copyright: " + pesPacket.getCopyright() + "\n" +
                                "Original or copy: " + pesPacket.getOriginalOrCopy() + "\n" +
                                "PTS DTS flags: " + pesPacket.getPTSdtsFlags() + "\n" +
                                "ES rate flag: " + pesPacket.getESrateFlag() + "\n" +
                                "DSM trick mode flag: " + pesPacket.getDSMtrickModeFlag() + "\n" +
                                "Additional copy info flag: " + pesPacket.getAdditionalCopyInfoFlag() + "\n" +
                                "PES CRC flag: " + pesPacket.getPEScrcFlag() + "\n" +
                                "PES extension flag: " + pesPacket.getPESextensionFlag() + "\n" +
                                "PES header data length: " + pesPacket.getPESheaderDataLength() + "\n" +
                                createPESoptionalFieldsOutput(pesPacket.getOptionalPESheader()) + "\n\n\n"
                );
            }
        }
        return "";
    }


    private String createPESoptionalFieldsOutput(PES.PESoptionalHeader optionalPESheader) {
        return ""; //TODO dorobit analyzu
    }


    private String createAdaptationFieldHeaderOutput(AdaptationFieldHeader adaptationFieldHeader) {
        if (adaptationFieldHeader != null) {
            return(
                    "Adaptation field \n\n" +
                            "Adaptation field length: " + adaptationFieldHeader.getAdaptationFieldLength() + "\n" +
                            "Discontinuity indicator: " + adaptationFieldHeader.getDI() + "\n" +
                            "Random access indicator: " + adaptationFieldHeader.getRAI() + "\n" +
                            "Elementary stream priority indicator: " + adaptationFieldHeader.getSPF()+ "\n" +
                            "PCR flag: " + adaptationFieldHeader.getPCRF()+ "\n" +
                            "OPCR flag: " + adaptationFieldHeader.getOPCRF() + "\n" +
                            "Splicing point flag: " + adaptationFieldHeader.getSPF() + "\n" +
                            "Transport private data flag: " + adaptationFieldHeader.getTPDF() + "\n" +
                            "Adaptation field extension flag: " + adaptationFieldHeader.getAFEF() + "\n" +
                            createAdaptationOtionalFieldsOutput(adaptationFieldHeader.getOptionalField()) + "\n\n\n"
            );
        }
        return "";
    }


    private String createAdaptationOtionalFieldsOutput(AdaptationFieldOptionalFields optionalField) {
        return ""; //TODO dorobit optional fields
    }



    private static String createDataOutput(byte[] bits) {

        BigInteger bigInt = new BigInteger(bits);
        String hexSequence = bigInt.toString(16);

        StringBuilder hexBuilder = new StringBuilder();
        hexBuilder.append( String.format("0x%06X   ", 0 & 0xFFFFF));

        int index = 0;
        for(char c : hexSequence.toCharArray()){
            hexBuilder.append(Character.toUpperCase(c));
            if(++index % 4 == 0){
                hexBuilder.append(" ");
            }
            if(index % 32 == 0){
                hexBuilder.append("\n");
                hexBuilder.append( String.format("0x%06X   ", (index/2) & 0xFFFFF));
            }
        }
        return (
                "Data: \n\n" +
                        "           0001 0203 0405 0607 0809 0A0B 0C0D 0E0F\n\n" +
                        hexBuilder.toString()
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
