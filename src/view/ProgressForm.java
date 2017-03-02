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
    private final Stage dialogStage = new Stage();
    private final ProgressBar pb = new ProgressBar();
    private final ProgressIndicator pin = new ProgressIndicator();
    private final VBox vbox = new VBox(5);
    private Label label = new Label();


    ProgressForm() {

        dialogStage.setTitle("Analyzing transport stream...");
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setResizable(false);
        dialogStage.setWidth(250);
        dialogStage.setHeight(100);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        label.setText(("Preparing stream..."));
        pb.setProgress(-1F);

        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(pin, label);

        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);
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
        pin.progressProperty().bind(task.progressProperty());
    }


    public void activatePinBar() {
        vbox.getChildren().clear();
        vbox.getChildren().addAll(pin);

        dialogStage.show();

        pin.indeterminateProperty();
    }

    public Stage getDialogStage() {
        return this.dialogStage;
    }

}