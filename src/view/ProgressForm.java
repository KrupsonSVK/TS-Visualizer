package view;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class ProgressForm {
    private final Stage dialogStage;
    private final ProgressBar pb;
    private final ProgressIndicator pin;
    private final VBox vbox;
    private Label label;


    ProgressForm(Stage dialogStage) {

        this.dialogStage = dialogStage;
        pb = new ProgressBar();
        pin = new ProgressIndicator();
        vbox = new VBox(5);
        label = new Label();

        this.dialogStage.setTitle("Analyzing transport stream...");
        this.dialogStage.initStyle(StageStyle.UTILITY);
        this.dialogStage.setResizable(false);
        this.dialogStage.setWidth(250);
        this.dialogStage.setHeight(100);
        //this.dialogStage.initModality(Modality.APPLICATION_MODAL);

        label.setText(("Preparing stream..."));
        pb.setProgress(-1F);

        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(pin, label);

        Scene scene = new Scene(vbox);
        this.dialogStage.setScene(scene);
    }


    public void activateProgressBar(final Task<?> task) {

        vbox.getChildren().clear();

        label.setText("Parsing packets");
        vbox.getChildren().addAll(pb, label);

        dialogStage.show();

        pin.progressProperty().unbind();
        pb.progressProperty().bind(task.progressProperty());
    }


    public void activatePinBar(final Task<?> task) {
        vbox.getChildren().clear();

        dialogStage.show();

        pb.progressProperty().unbind();
        //pin.progressProperty().bind(task.progressProperty());
        pin.indeterminateProperty();

    }


    public Stage getDialogStage() {
        return this.dialogStage;
    }

}