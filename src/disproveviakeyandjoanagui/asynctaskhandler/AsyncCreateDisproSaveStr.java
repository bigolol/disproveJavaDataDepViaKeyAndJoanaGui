/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui.asynctaskhandler;

import disproveviakeyandjoanagui.CurrentActionLogger;
import disproveviakeyandjoanagui.ErrorLogger;
import java.io.FileNotFoundException;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        
    public void createSaveStr(DisprovingProject disprovingProject, BiConsumer<String, Boolean> uiThreadCallback) {
        this.disprovingProject = disprovingProject;
        this.uiThreadCallback = uiThreadCallback;
        actionLogger.startProgress("now creating save file from project, pls wait k thx");
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
            ErrorLogger.logError("error when trying to create save str for disprovingProject",
                    ErrorLogger.ErrorTypes.ERROR_CREATING_SAVE_STR_FOR_DISPRO);
        }
        Platform.runLater(() -> {
            actionLogger.endProgress();
            uiThreadCallback.accept(saveStr, succes);
        });
    }
}
