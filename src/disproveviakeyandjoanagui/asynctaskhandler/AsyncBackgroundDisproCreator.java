/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui.asynctaskhandler;

import disproveviakeyandjoanagui.CurrentActionLogger;
import disproveviakeyandjoanagui.ErrorLogger;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import joanakeyrefactoring.JoanaAndKeyCheckData;
import joanakeyrefactoring.persistence.DisprovingProject;

/**
 *
 * @author holger
 */
public class AsyncBackgroundDisproCreator implements Runnable {

    private JoanaAndKeyCheckData checkData;
    private BiConsumer<DisprovingProject, Boolean> callback;
    private CurrentActionLogger currentActionLogger;
    DisprovingProject disprovingProject;
    Boolean loadingWorked = false;

    public AsyncBackgroundDisproCreator(CurrentActionLogger currentActionLogger) {
        this.currentActionLogger = currentActionLogger;
    }

    public void generateFromCheckData(JoanaAndKeyCheckData checkData, BiConsumer<DisprovingProject, Boolean> callback) {
        this.callback = callback;
        this.checkData = checkData;
        currentActionLogger.startProgress("generating Dispro Project from jaok file, this might take a while ...");
        new Thread(this).start();
    }

    @Override
    public void run() {
       try {
            disprovingProject = DisprovingProject.generateFromCheckdata(checkData);
            loadingWorked = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorLogger.logError("could not create dispro proj from joak file for some reason", ErrorLogger.ErrorTypes.ERROR_PARSING_JOAK);
        }
        Platform.runLater(() -> {
            currentActionLogger.endProgress();
            callback.accept(disprovingProject, loadingWorked);
        });
    }

}
