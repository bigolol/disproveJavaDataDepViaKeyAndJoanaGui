/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui.asynctaskhandler;

import disproveviakeyandjoanagui.CurrentActionLogger;
import disproveviakeyandjoanagui.ErrorLogger;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import joanakeyrefactoring.persistence.DisprovingProject;

/**
 *
 * @author holger
 */
public class AsyncCreateDisproSaveStr implements Runnable {
    private String saveStr;
    private boolean succes = false;
    private DisprovingProject disprovingProject;
    private BiConsumer<String, Boolean> uiThreadCallback;
    private CurrentActionLogger actionLogger;

    public AsyncCreateDisproSaveStr(CurrentActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }
        
    public void createSaveStr(DisprovingProject disprovingProject,
                              BiConsumer<String, Boolean> uiThreadCallback) {
        this.disprovingProject = disprovingProject;
        this.uiThreadCallback = uiThreadCallback;
        actionLogger.startProgress("Now creating storage file from project, please wait.");
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {            
            disprovingProject.saveSDG();
            saveStr = disprovingProject.generateSaveString();
            succes = true;
        } catch (Exception ex) {
            succes = false;
            ErrorLogger.logError("Error while trying to create storage String for disprovingProject.",
                    ErrorLogger.ErrorTypes.ERROR_CREATING_SAVE_STR_FOR_DISPRO);
        }
        Platform.runLater(() -> {
            actionLogger.endProgress();
            uiThreadCallback.accept(saveStr, succes);
        });
    }
}
