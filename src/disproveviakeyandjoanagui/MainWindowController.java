/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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

    //---------------------other fields---------------------------
    public enum ErrorTypes {
        ERROR_USER_CHOOSING_FILE
    }
    
    private static FileChooser fileChooser;

    private static ArrayList<String> errorMsgs = new ArrayList<>();
    private static ArrayList<ErrorTypes> errorTypes = new ArrayList<>();

    private Stage mainStage;

    //---------------------static methods boiiiiii-----------------------
    public static File letUserChooseFile(String title, String extensionExp, String extension, File baseDirectory, Window ownerWindow) {
        fileChooser.setTitle(title);
        if (baseDirectory != null) {
            fileChooser.setInitialDirectory(baseDirectory);
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(extensionExp, extension)
        );
        File chosenFile = fileChooser.showOpenDialog(ownerWindow);
        return chosenFile;
    }    

    public static void logError(String msg, ErrorTypes errorType) {
        errorMsgs.add(msg);
        errorTypes.add(errorType);
        System.out.println("Error: " + msg);
    }
    
    //-------------------non-static methods-------------------------------

    private void tryLetUserChooseJoakFileAndHandleResponse() {
        String title = "please navigate to and select .joak file";
        String extensionExp = "A .joak file containing info about which java project to load";
        String extension = ".joak";
        File joakFile = letUserChooseFile(title, extensionExp, extension, null, mainStage);
        if (joakFile == null) {
            logError("new joak file was chosen by user or the file was chosen incorrectly or another error (such as IO) occured, homeboy", 
                    ErrorTypes.ERROR_USER_CHOOSING_FILE);
        } else {
            
        }
    }
    
    

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

}
