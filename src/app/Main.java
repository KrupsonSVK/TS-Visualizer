package app;

import javafx.application.Application;
import javafx.stage.Stage;
import model.config.Localization;
import view.Window;

import static model.config.Config.localizationEN;

public class Main extends Application {

    public Window view;
    public Controller controller;
    public static Localization localization;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        localization = localizationEN;
        view = new Window(primaryStage);
        controller = new Controller( primaryStage, view );
    }
}
