package view.graphTabs;


import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import model.Stream;

public interface Graph {
    void drawGraph(Stream stream);
    void addListenersAndHandlers(Stream stream, Chart chart);
    void setScene(Scene scene);
}
