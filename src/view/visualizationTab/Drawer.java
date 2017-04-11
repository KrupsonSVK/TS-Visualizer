package view.visualizationTab;


import javafx.scene.input.MouseEvent;
import model.Stream;
import model.TSpacket;

import java.util.ArrayList;
import java.util.Map;

public interface Drawer {

    void updateX(MouseEvent mouseEvent);
    void setXpos(double xpos);
    void createScrollPane(Stream stream, ArrayList<TSpacket> packets, Map sortedPIDs, int lines);
    double translate(double sceneX);
    double getLookingGlassMoveCoeff();
    double stayInRange(double xPos);
    void drawCanvas(Stream stream, ArrayList<TSpacket> packets, double xPos);
    void drawPackets(Stream stream, ArrayList<TSpacket> packets, double xPos);
    }
