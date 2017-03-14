package view.visualizationTab;


import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import model.Stream;
import model.TSpacket;

import java.util.ArrayList;
import java.util.List;

public interface Drawer {


    double translate(double sceneX);
    void updateX(MouseEvent mouseEvent);
    void setXpos(double xpos);
    void setOldTranslateX(double oldTranslateX);
    void setOldSceneX(double oldSceneX);
    void createScrollPane(Stream stream, ArrayList<TSpacket> packets, List sortedPIDs, int lines);
}
