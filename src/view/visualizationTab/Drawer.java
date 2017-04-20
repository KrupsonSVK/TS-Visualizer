package view.visualizationTab;


import javafx.scene.input.MouseEvent;
import model.Stream;
import model.packet.Packet;

import java.util.ArrayList;
import java.util.Map;

public interface Drawer {

    void updateX(MouseEvent mouseEvent);
    void setXpos(double xpos);
    void createScrollPane(Stream stream, ArrayList<Packet> packets, Map sortedPIDs, int lines);
    double translate(double sceneX);
    double getLookingGlassMoveCoeff();
    double stayInRange(double xPos);
    void drawCanvas(Stream stream, ArrayList<Packet> packets, double xPos);
    void drawPackets(Stream stream, ArrayList<Packet> packets, double xPos);
    }
