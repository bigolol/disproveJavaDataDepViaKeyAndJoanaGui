/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import joanakeyrefactoring.CombinedApproach;
import joanakeyrefactoring.JoanaAndKeyCheckData;

/**
 *
 * @author holger
 */
public class JoakFileManager {

    @FXML
    private Label labelProjName;

    private File currentJoakFile;
    private CurrentActionLogger actionLogger;
    private JoanaAndKeyCheckData currentCheckData;

    public JoakFileManager(Label labelProjName, CurrentActionLogger actionLogger) {
        this.labelProjName = labelProjName;
        this.actionLogger = actionLogger;
    }

    public void handleJoakFileChanged(File newFile) {
        currentJoakFile = newFile;
        try {
            actionLogger.startProgress("now parsing the joak file");
            currentCheckData = CombinedApproach.parseInputFile(newFile.getAbsolutePath());
        } catch (Exception e) {
            ErrorLogger.logError("CombinedApproach threw while trying to pass the joak file " + newFile.getName(),
                    ErrorLogger.ErrorTypes.ERROR_PARSING_JOAK);
            actionLogger.endProgress();
            return;
        }
        actionLogger.endProgress();
        labelProjName.setText(newFile.getAbsolutePath());
    }

}
