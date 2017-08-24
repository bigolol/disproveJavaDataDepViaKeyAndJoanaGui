/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

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
    //---------------------other fields---------------------------

    private static FileChooser fileChooser = new FileChooser();
    private Stage mainStage;
    private LoopInvariantFromUserGetter loopInvariantGetter = new LoopInvariantFromUserGetter();

    private JoakFileManager joakFileManager;
    private DisproFileManager disproFileManager;

    final private String disprovingProgressFileEnding = "dispro";
    final private String projectFileEnding = "joak";

    private CurrentActionLogger actionLogger;

    //---------------------static methods boiiiiii-----------------------
    public static File letUserChooseFile(String title, String extensionExp, String extension, File baseDirectory, Window ownerWindow) {
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
        if (!joakordispro.equals(disprovingProgressFileEnding) && !joakordispro.equals(projectFileEnding)) {
            ErrorLogger.logError("the file extension is not known to this program. Pls step yo game up",
                    ErrorLogger.ErrorTypes.UNKNOWN_FILE_EXTENSION);
            return;
        }
        String title = "please navigate to and select " + joakordispro + " file";
        String extensionExp = "A .joak file containing info about which java project to load";
        String extension = "*." + joakordispro;
        File file = letUserChooseFile(title, extensionExp, extension, null, mainStage);
        if (file == null) {
            ErrorLogger.logError("no" + joakordispro
                    + "file was chosen by user or the file was chosen incorrectly or another error (such as IO) occured, homeboy",
                    ErrorLogger.ErrorTypes.ERROR_USER_CHOOSING_FILE);
        } else {
            if (joakordispro.equals(projectFileEnding)) {
                joakFileManager.handleJoakFileChanged(file);
            } else if (joakordispro.equals(disprovingProgressFileEnding)) {
                disproFileManager.handleNewDisproFile(file);
            }
        }
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        actionLogger = new CurrentActionLogger(labelCurrentAction, progressIndicator);
        joakFileManager = new JoakFileManager(labelProjName, actionLogger);

        menuItemOpenJoak.setOnAction((event) -> {
            tryLetUserChooseFileAndHandleResponse(projectFileEnding);
        });
        menuItemOpenDispro.setOnAction((event) -> {
            tryLetUserChooseFileAndHandleResponse(disprovingProgressFileEnding);
        });
    }

}
