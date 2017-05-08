package view;

import app.streamAnalyzer.TimestampParser;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Stream;
import model.config.Localization;
import view.graphTabs.BitrateTab;
import view.graphTabs.CompositionTab;
import view.graphTabs.StructureTab;
import view.graphTabs.TimestampsTab;
import view.visualizationTab.VisualizationTab;

import java.io.IOException;

import static app.Main.localization;
import static model.config.Config.*;

public class Window extends TimestampParser {

    public Stage primaryStage;
    public Stage miniStage;
    public Button setButton;
    public FileChooser saveFileChooser;
    private Stage userGuideStage;
    private DetailTab detailTab;
    private BitrateTab bitrateTab;
    private StructureTab structureTab;
    private CompositionTab compositionTab;
    private VisualizationTab visualizationTab;
    private TimestampsTab timestampsTab;
    public FileChooser openFileChooser;
    public Button selectFileButton;
    public Scene scene;
    private StackPane rootPane;
    private BorderPane borderPane;
    public ProgressDialog progressWindow;
    private Alert alertBox;
    private VBox topContainer;
    private MenuBar mainMenu;
    private ToolBar toolBar;
    private TabPane tabPane;
    private Menu file, edit, help;
    public MenuItem openFile;
    public MenuItem importXML;
    public MenuItem exportXML;
    public MenuItem exportTXT;
    public MenuItem settings;
    public MenuItem exitApp;
    private MenuItem jumpToPacket;
    private MenuItem searchPacket;
    public MenuItem about;
    public MenuItem userGuide;

    private Task task;



    public Window(Stage primaryStage) {

        primaryStage.getIcons().add((Image) typeIcons.get(DVBicon));

        this.task = null;

        this.primaryStage = primaryStage;
        miniStage = new Stage();
        userGuideStage = new Stage();
        Stage progressStage = new Stage();

        rootPane = new StackPane();
        GridPane gridPane = new GridPane();
        borderPane = new BorderPane();

        topContainer = new VBox();
        mainMenu = new MenuBar();
        toolBar = new ToolBar();

        tabPane = new TabPane();

        createToolBar();

        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(windowGripGap);
        gridPane.setVgap(windowGripGap);
        gridPane.setPadding(windowInsets);

        topContainer.getChildren().add(mainMenu);
        borderPane.setTop(topContainer);

        rootPane.setStyle(windowStyle);

        selectFileButton.setAlignment(Pos.CENTER);
        gridPane.getChildren().addAll(selectFileButton);

        rootPane.getChildren().addAll(gridPane, borderPane);
        borderPane.setCenter(gridPane);

        alertBox = new Alert(Alert.AlertType.ERROR);
        progressWindow = new ProgressDialog(progressStage);

        scene = new Scene(rootPane, windowWidth, windowHeight);
        primaryStage.setTitle(localization.getSoftwareName());
        primaryStage.setScene(scene);
        primaryStage.show();

        detailTab = new DetailTab();
        visualizationTab = new VisualizationTab();
        bitrateTab = new BitrateTab();
        structureTab = new StructureTab();
        compositionTab = new CompositionTab();
        timestampsTab = new TimestampsTab();

        detailTab.setScene(scene);
        visualizationTab.init(scene);
        bitrateTab.setScene(scene);
        structureTab.setScene(scene);
        compositionTab.setScene(scene);
        timestampsTab.setScene(scene);

        setButton = new Button();

        mainMenu.toFront();
    }


    public Window() {
    }


    private void createToolBar() {


        openFileChooser = new FileChooser();
        saveFileChooser = new FileChooser();
        saveFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));

        selectFileButton = new Button(localization.getSelectFileText());

        file = new Menu(localization.getFileMenuText());
        edit = new Menu(localization.getEditMenuText());
        help = new Menu(localization.getHelpMenuText());
        edit.setDisable(true);

        openFile = new MenuItem(localization.getOpenFileMenuText());
        importXML = new MenuItem(localization.getImportXMLMenuText());
        exportXML = new MenuItem(localization.getExportXMLMenuText());
        exportTXT = new MenuItem(localization.getExportTXTMenuText());
        settings = new MenuItem(localization.getSettingsMenuText());
        exitApp = new MenuItem(localization.getExitMenuText());

        jumpToPacket = new MenuItem(localization.getJumpMenuText());
        searchPacket = new MenuItem(localization.getSearchMenuText());

        about = new MenuItem(localization.getAboutMenuText());
        userGuide = new MenuItem(localization.getGuideMenuText());

        importXML.setDisable(true);
        exportXML.setDisable(true);
        exportTXT.setDisable(true);
        settings.setDisable(false);
        jumpToPacket.setDisable(true);
        searchPacket.setDisable(true);

        file.getItems().addAll(openFile,  exportXML, exportTXT, new SeparatorMenuItem(), settings, new SeparatorMenuItem(), exitApp);
        edit.getItems().addAll(jumpToPacket, searchPacket);
        help.getItems().addAll(userGuide, new SeparatorMenuItem(), about);

        mainMenu.getMenus().addAll(file, edit, help);
    }


    public void createTask(Stream streamDescriptor) {

        this.task = new Task() {
            @Override
            public Object call() throws InterruptedException, IOException {
                Platform.runLater(() -> {
                    detailTab.createTreeTab(streamDescriptor);
                    visualizationTab.visualizePackets(streamDescriptor);
                    drawGraphs(streamDescriptor);
                    createTabPane();
                });
                rootPane.setStyle(afterWindowStyle);
                exportTXT.setDisable(false);
                return null;
            }
        };
    }


    private void drawGraphs(Stream streamDescriptor) {
        bitrateTab.drawGraph(streamDescriptor);
        structureTab.drawGraph(streamDescriptor);
        compositionTab.drawGraph(streamDescriptor);
        timestampsTab.drawGraph(streamDescriptor);
    }


    public void showAlertBox(String title, String content) {
        alertBox.setTitle(title);
        alertBox.setContentText(content);
        alertBox.setHeaderText(null);
        alertBox.show();
    }


    public void createTabPane() {
        tabPane.getTabs().addAll(detailTab.tab, visualizationTab.tab, bitrateTab.tab, structureTab.tab, compositionTab.tab, timestampsTab.tab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        borderPane.setCenter(tabPane);
        rootPane.getChildren().clear();
        rootPane.getChildren().addAll(borderPane);
    }


    public void showAbout() {
        if(miniStage.getModality() != Modality.WINDOW_MODAL) {
            miniStage.initModality(Modality.WINDOW_MODAL);
        }
        miniStage.setTitle(localization.getAboutTitle());

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(vBoxInsets);

        Text text = new Text(logoText);
        text.setFont(Font.font("Monospaced", 6));
        vBox.getChildren().addAll(
                text,
                new Text(localization.getSoftwareName() + version),
                new Text("\n\n" + localization.getReleaseText()  + releaseDate ),
                new Text("\n" + localization.getCreatedByText()),
                new Text("\n" + email + "\n"),
                new Label(localization.getRightsText())
        );

        miniStage.setScene(new Scene(vBox, aboutWidth, aboutHeight));
        miniStage.show();
    }


    public void showSettings() {
        if(miniStage.getModality() != Modality.WINDOW_MODAL) {
            miniStage.initModality(Modality.WINDOW_MODAL);
        }
        //miniStage.setTitle(localization.getSettingsTitle());
        miniStage.setTitle(localization.getSettingsText());

        Label languagelabel = new Label(localization.getLanguage() +": ");

        LanguageButton slovak = new LanguageButton("Slovak", localizationSK);
        LanguageButton english = new LanguageButton("English", localizationEN);
        LanguageButton deutsch = new LanguageButton("Deutsch", localizationDE);
        LanguageButton russian = new LanguageButton("Руский", localizationRU);

        ToggleGroup radioButtonsGroup = new ToggleGroup();
        slovak.setToggleGroup(radioButtonsGroup);
        english.setToggleGroup(radioButtonsGroup);
        deutsch.setToggleGroup(radioButtonsGroup);
        russian.setToggleGroup(radioButtonsGroup);

        deutsch.setDisable(true);
        russian.setDisable(true);

        setButton.setDisable(true);
        setButton.setText(localization.getButtonSetText());

        for(Object button : radioButtonsGroup.getToggles()){
            if(((LanguageButton)button).getText().equals(localization.getLocalization())){
                ((LanguageButton) button).setSelected(true);
                ((LanguageButton) button).setOnAction(event -> setButton.setDisable(true));
                break;
            }
        }

        radioButtonsGroup.selectedToggleProperty().addListener((observable, oldVal, newVal) -> {
            try {
                localization = ((LanguageButton)newVal).getLocalization();
                miniStage.setTitle(localization.getSettingsText());
                setButton.setText(localization.getButtonSetText());
                settings.setText(localization.getSettingsText());
                languagelabel.setText(localization.getLanguage() +": ");
                if(!localization.getLanguage().equals(setButton.getText())) {
                    setButton.setDisable(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        HBox buttonsHBox = new HBox(slovak,english,deutsch,russian);
        buttonsHBox.setAlignment(Pos.CENTER);
        buttonsHBox.setPadding(hBoxInsets);
        buttonsHBox.setSpacing(chartHBoxSpacing);
        VBox vBox = new VBox(languagelabel,buttonsHBox, setButton);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(hBoxInsets);
        vBox.setSpacing(chartHBoxSpacing);

        miniStage.setScene(new Scene(vBox, aboutWidth, aboutHeight));
        miniStage.show();
    }


    public void showUserGuide() {
        if(userGuideStage.getModality() != Modality.WINDOW_MODAL) {
            userGuideStage.initModality(Modality.WINDOW_MODAL);
        }
        userGuideStage.setTitle(localization.getUserGuideTitle());
        TextArea textArea = new TextArea(localization.getUserGuideText());
        textArea.setWrapText(true);
        textArea.setMinHeight(textAreaMinHeigth);
        textArea.setEditable(false);

        Label label = new Label(localization.getUserGuideLabel() + "\n");
        label.setFont(new Font(labelFontSize));

        VBox vBox = new VBox(label,textArea);
        vBox.setMargin(label,labelInsets);
        vBox.setMargin(textArea,textInsets);
        vBox.setAlignment(Pos.CENTER);
        userGuideStage.setScene(new Scene(vBox, userGuideWidth, userGuideHeight));
        userGuideStage.show();
    }


    public Task getTask() {
        return task;
    }


    public TreeItem getTreeData() {
        return detailTab.getTreeData();
    }


    private class LanguageButton extends RadioButton{
        private Localization localization;

        public LanguageButton(String name, Localization localization) {
            super(name);
            this.localization = localization;
        }

        public Localization getLocalization() {
            return localization;
        }
    }
}