package app;

import javafx.application.Application;
import javafx.stage.Stage;
import view.Window;


public class Main extends Application {

    public static final String releaseDate = "01.04.2017";

    public Window view;
    public Controller controller;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        view = new Window(primaryStage);
        controller = new Controller( primaryStage, view );
    }
}
