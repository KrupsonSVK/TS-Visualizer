package app;

import app.streamAnalyzer.StreamParser;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;

import model.Stream;
import view.Window;


public class Controller {

    private static final String errorTitle = "Error occured!";
    private XML XML;

    private Thread streamParserThread, fileHandlerThread, visualizerThread;
    private Stage primaryStage;
    private FileHandler fileHandler;
    private Window view;
    private StreamParser streamParser;

    private Stream stream;


    Controller(Stage primaryStage, Window view) {

        this.primaryStage = primaryStage;
        this.view = view;
        this.streamParser = new StreamParser(null);
        this.stream = null;
        this.XML = null;

        try {
            this.fileHandler = new FileHandler();
        } catch (IOException error) {
            error.printStackTrace();
            this.view.showAlertBox(errorTitle, String.valueOf(error.getMessage()));
        }


        this.view.scene.setOnDragOver(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            if (db.hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            } else {
                dragEvent.consume();
            }
        });

        this.view.selectFileButton.setOnAction(actionEvent -> {
            File file = this.view.fileChooser.showOpenDialog(this.view.primaryStage);
            if (file != null) {
                openFile(file);
            }
        });

        this.view.scene.setOnDragDropped(dragEvent -> {
            Dragboard dragboard = dragEvent.getDragboard();
            if (dragboard.hasFiles()) {
                openFile(dragboard.getFiles().get(0));
            }
            dragEvent.setDropCompleted(dragboard.hasFiles());
            dragEvent.consume();
        });

        this.view.exitApp.setOnAction(actionEvent -> {
            System.exit(0);
        });


        this.view.openFile.setOnAction(actionEvent -> {
            File file = this.view.fileChooser.showOpenDialog(this.view.primaryStage);
            if (file != null) {
                openFile(file);
            }
        });

        this.view.userGuide.setOnAction(actionEvent ->  {
            this.view.showUserGuide();
        });

        this.view.about.setOnAction(actionEvent ->  {
            this.view.showAbout();
        });

        this.view.importXML.setOnAction(actionEvent -> {
                    File file = this.view.fileChooser.showOpenDialog(this.view.primaryStage);
                    if (file != null) {
                        try {
                            this.view.createTask(XML.read(file));
                        } catch (Exception error) {
                            error.printStackTrace();
                            this.view.showAlertBox(errorTitle, String.valueOf(error.getMessage()));
                        }
                    }
                }
        );

        this.view.exportXML.setOnAction(actionEvent -> {
                    File file = this.view.fileChooser.showOpenDialog(this.view.primaryStage);
                    if (file != null) {
                        try {
                            this.XML.save(this.stream, file);
                        } catch (Exception error) {
                            error.printStackTrace();
                            this.view.showAlertBox(errorTitle, String.valueOf(error.getMessage()));
                        }
                    }
                }
        );
    }


    void createTask(File file) {

        fileHandler.getTask().setOnSucceeded(event -> {

            streamParser.parseStream(fileHandler.getTask().getValue());

            view.progressWindow.activateProgressBar(streamParser.getTask());

            streamParser.getTask().setOnSucceeded(workerStateEvent -> {
                        Stream streamDescriptor;
                        try {
                            streamDescriptor = streamParser.analyzeStream(file, streamParser.getTask().getValue());
                            view.createTask(streamDescriptor);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.showAlertBox(errorTitle, String.valueOf(e.getMessage()));
                        }
                        view.scene.setOnMouseClicked(mouseEvent -> { System.out.println("scene x: " + view.scene.getX() + "  scene y: " + view.scene.getY() + "mouse x: " + mouseEvent.getX() + "  mouse y: " + mouseEvent.getY());});

                        visualizerThread = new Thread(view.getTask());
                        visualizerThread.start();

                        view.progressWindow.getDialogStage().close();
                    }
            );

            streamParser.getTask().setOnFailed(workerStateEvent -> {
                        view.progressWindow.getDialogStage().close();
                        streamParser.getTask().getException().printStackTrace();
                        view.showAlertBox(errorTitle, String.valueOf(streamParser.getTask().getException().getStackTrace()) + streamParser.getTask().getException().getMessage());
                    }
            );

            streamParserThread = new Thread(streamParser.getTask());
            streamParserThread.start();
        });

        fileHandler.getTask().setOnFailed(workerStateEvent ->
                {
                    view.progressWindow.getDialogStage().close();
                    fileHandler.getTask().setOnFailed(event -> {
                        fileHandler.getTask().getException().printStackTrace();
                        view.showAlertBox(errorTitle, fileHandler.getTask().getException().getMessage());
                    });
                }
        );
    }


    private void openFile(File file) {


        try {
            fileHandler.createTask(file);
        } catch (IOException e) {
            e.printStackTrace();
            view.showAlertBox(errorTitle, e.getMessage());
        }

        view.progressWindow.activatePinBar(this.fileHandler.getTask());

        createTask(file);

        fileHandlerThread = new Thread(this.fileHandler.getTask());
        fileHandlerThread.start();

        view.progressWindow.getDialogStage().setOnCloseRequest(windowEvent ->   {
            try {
                this.restart();
            } catch (IOException error) {
                error.printStackTrace();
                this.view.showAlertBox(errorTitle, String.valueOf(error.getMessage()));
            }
        });
    }


    private void restart () throws IOException { //TODO not working

        if (fileHandlerThread != null) {
            fileHandler.getTask().cancel();
            fileHandlerThread.interrupt();
            fileHandler = null;
            fileHandlerThread = null;

            if (streamParserThread != null) {
                streamParser.getTask().cancel();
                streamParserThread.interrupt();
                streamParser = null;
                streamParserThread = null;

                System.gc();
                streamParser = new StreamParser(null);
                fileHandler= new FileHandler();
            }
        }
    }
}