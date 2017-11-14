/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import disproveviakeyandjoanagui.asynctaskhandler.AsyncBackgroundLoader;
import disproveviakeyandjoanagui.asynctaskhandler.AsyncCreateDisproSaveStr;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import joanakeyrefactoring.persistence.DisprovingProject;

/**
 *
 * @author holger
 */
public class MainWindowController implements Initializable {

    //---------------------fxml fields---------------------------
    @FXML
    private AnchorPane anchorPaneBasicInfo;

    @FXML
    private Label labelProjName;

    @FXML
    private Label labelCurrentAction;

    @FXML
    private Label labelSummaryEdge;

    @FXML
    private Label labelSomeOtherData;

    @FXML
    private AnchorPane anchorPaneDisproProgress;

    @FXML
    private MenuItem menuItemOpenJoak;

    @FXML
    private MenuItem menuItemOpenDispro;

    @FXML
    private MenuItem menuItemSaveProgress;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private AnchorPane anchorPaneMain;

    @FXML
    private MenuBar menuBarMain;

    @FXML
    private ListView<String> listViewUncheckedChops;

    @FXML
    private ListView<String> listViewUncheckedEdges;

    @FXML
    private ListView<String> listViewLoopsOfSE;

    @FXML
    private ListView<String> listViewCalledMethodsOfSE;

    @FXML
    private ListView<String> listViewFormalInoutPairs;

    @FXML
    private AnchorPane anchorPaneMethodCode;

    @FXML
    private AnchorPane anchorPaneLoopInvariant;

    @FXML
    private AnchorPane anchorPaneKeyContract;

    @FXML
    private Button buttonMarkAsDisproved;

    @FXML
    private Button buttonOpenSelected;

    @FXML
    private Button buttonTryDisprove;

    @FXML
    private Button buttonRunAtuo;

    @FXML
    private Button buttonSaveLoopInvariant;

    @FXML
    private Button buttonResetLoopInvariant;

    //---------------------other fields---------------------------
    private static FileChooser fileChooser = new FileChooser();
    private Stage mainStage;
    //private LoopInvariantFromUserGetter loopInvariantGetter = new LoopInvariantFromUserGetter();
    private AsyncBackgroundLoader asyncBackgroundLoader;

    final private static String DISPROVING_PROGRESS_FILE_ENDING = "dispro";
    final private static String PROJECT_FILE_ENDING = "joak";

    private DisproHandler disproHandler;
    private AsyncCreateDisproSaveStr disproSaveStrCreator;

    private CurrentActionLogger actionLogger;

    //---------------------static methods boiiiiii-----------------------
    public static File letUserChooseFile(String title, String extensionExp, String extension,
                                         File baseDirectory, Window ownerWindow) {
        fileChooser.setTitle(title);
        if (baseDirectory != null) {
            fileChooser.setInitialDirectory(baseDirectory);
        }
        fileChooser.setSelectedExtensionFilter(
                new FileChooser.ExtensionFilter(extensionExp, extension)
        );
        File chosenFile = fileChooser.showOpenDialog(ownerWindow);
        return chosenFile;
    }

    //-------------------non-static methods-------------------------------
    private void tryLetUserChooseFileAndHandleResponse(String joakordispro) {
        if (!joakordispro.equals(DISPROVING_PROGRESS_FILE_ENDING)
                && !joakordispro.equals(PROJECT_FILE_ENDING)) {
            ErrorLogger.logError("This file extension is not known to the program. " +
                                 "Please step yo game up.",
                                 ErrorLogger.ErrorTypes.UNKNOWN_FILE_EXTENSION);
            return;
        }
        String title = "Please navigate to and select " + joakordispro + " file.";
        String extensionExp = "A *.joak file containing information about which Java project to load.";
        String extension = "*." + joakordispro;
        File file = letUserChooseFile(title, extensionExp, extension, null, mainStage);
        if (file == null) {
            ErrorLogger.logError("No" + joakordispro + ". "
                    + "File was chosen by user or the file was chosen incorrectly "
                    + "or another error (such as IO) occurred, homeboy.",
                    ErrorLogger.ErrorTypes.ERROR_USER_CHOOSING_FILE);
        } else {
            //TODO: make only certain control elements inactive, or handle user clicking in some other way
            //might be more trouble than its worth though...idk
            menuBarMain.setDisable(true);
            if (joakordispro.equals(PROJECT_FILE_ENDING)) {
                disproHandler.setAllButtonsDisable(true);
                asyncBackgroundLoader.loadJoakFile(file, (newCheckData, success) -> {
                    if (success) {
                        disproHandler.handleNewDispro(newCheckData);
                    } else {
                        menuBarMain.setDisable(false);
                    }
                });
            } else if (joakordispro.equals(DISPROVING_PROGRESS_FILE_ENDING)) {
                disproHandler.setAllButtonsDisable(true);
                asyncBackgroundLoader.loadDisproFie(file, (dispro, succes) -> {
                    if (succes) {
                        disproHandler.handleNewDispro(dispro);
                    } else {
                        menuBarMain.setDisable(false);
                    }
                });
            }
        }
    }

    private void saveDispro() {
        DisprovingProject disprovingProject = disproHandler.getDisprovingProject();
        disproSaveStrCreator.createSaveStr(disprovingProject, (String string, Boolean sucess) -> {
            if (sucess) {
                fileChooser.setTitle("Please navigate to where you want to save this disproving project.");
                fileChooser.setSelectedExtensionFilter(
                        new FileChooser.ExtensionFilter("Disproving project storage file", ".dispro"));
                File saveFile = fileChooser.showSaveDialog(mainStage);
                if (saveFile == null) {
                    ErrorLogger.logError("No file was chosen by user or the file was chosen incorrectly or " +
                                         "another error (such as IO) occured, homeboy.",
                                         ErrorLogger.ErrorTypes.ERROR_USER_CHOOSING_FILE);
                } else {
                    if (!saveFile.getName().endsWith(".dispro")) {
                        saveFile = new File(saveFile.getAbsolutePath() + ".dispro");
                    }
                    FileWriter fileWriter;
                    try {
                        fileWriter = new FileWriter(saveFile);
                        fileWriter.write(string);
                        fileWriter.close();
                    } catch (IOException ex) {
                        ErrorLogger.logError("Error while trying to write file to disk.",
                                ErrorLogger.ErrorTypes.ERROR_WRITING_FILE_TO_DISK);
                    }
                }
            }
        });
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        actionLogger = new CurrentActionLogger(labelCurrentAction, progressIndicator);
        asyncBackgroundLoader = new AsyncBackgroundLoader(actionLogger);
        disproHandler = new DisproHandler(
                actionLogger,
                labelProjName,
                labelSummaryEdge,
                labelSomeOtherData,
                menuBarMain,
                listViewUncheckedEdges,
                listViewUncheckedChops,
                listViewCalledMethodsOfSE,
                listViewLoopsOfSE,
                listViewFormalInoutPairs,
                anchorPaneMethodCode,
                anchorPaneLoopInvariant,
                anchorPaneKeyContract,
                buttonSaveLoopInvariant,
                buttonResetLoopInvariant,
                buttonMarkAsDisproved,
                buttonOpenSelected,
                buttonTryDisprove,
                buttonRunAtuo);
        disproHandler.setAllButtonsDisable(true);
        disproSaveStrCreator = new AsyncCreateDisproSaveStr(actionLogger);

        menuItemOpenJoak.setOnAction((event) -> {
            tryLetUserChooseFileAndHandleResponse(PROJECT_FILE_ENDING);
        });
        menuItemOpenDispro.setOnAction((event) -> {
            tryLetUserChooseFileAndHandleResponse(DISPROVING_PROGRESS_FILE_ENDING);
        });
        menuItemSaveProgress.setOnAction((event) -> {
            saveDispro();
        });
    }

}
