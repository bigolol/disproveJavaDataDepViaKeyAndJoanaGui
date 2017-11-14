/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui.asynctaskhandler;

import disproveviakeyandjoanagui.CurrentActionLogger;
import disproveviakeyandjoanagui.ErrorLogger;
import java.io.File;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import joanakeyrefactoring.CombinedApproach;
import joanakeyrefactoring.JoanaAndKeYCheckData;
import joanakeyrefactoring.persistence.DisprovingProject;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author holger
 */
public class AsyncBackgroundLoader implements Runnable {

    private File fileToLoad;
    private LoadOptions loadOption;
    private CurrentActionLogger actionLogger;
    private boolean success;
    private JoanaAndKeYCheckData currentCheckData;
    private BiConsumer<JoanaAndKeYCheckData, Boolean> uiThreadJoakCallback;
    private DisprovingProject disprovingProject;
    private BiConsumer<DisprovingProject, Boolean> uiThreadDisproCallback;

    private enum LoadOptions {
        JOAK, DISPRO
    }

    public AsyncBackgroundLoader(CurrentActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void loadJoakFile(File f, BiConsumer<JoanaAndKeYCheckData, Boolean> callback) {
        this.fileToLoad = f;
        this.uiThreadJoakCallback = callback;
        loadOption = LoadOptions.JOAK;
        actionLogger.startProgress(
                "Now parsing the JOAK file, this might take a while to build the SDG ...");
        new Thread(this).start();
    }

    public void loadDisproFie(File f, BiConsumer<DisprovingProject, Boolean> callback) {
        this.fileToLoad = f;
        this.uiThreadDisproCallback = callback;
        loadOption = LoadOptions.DISPRO;
        actionLogger.startProgress("Now parsing the DISPRO file, this might take a while ...");
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (loadOption == LoadOptions.JOAK) {
            try {
                currentCheckData = CombinedApproach.parseInputFile(fileToLoad);
                success = true;
            } catch (Exception ex) {
                success = false;
                ErrorLogger.logError("CombinedApproach threw " + ex.getClass().getSimpleName() +
                                     " while trying to pass the JOAK file " + fileToLoad.getName() + ".",
                                     ErrorLogger.ErrorTypes.ERROR_PARSING_JOAK);
            }
            Platform.runLater(() -> {
                actionLogger.endProgress();
                uiThreadJoakCallback.accept(currentCheckData, success);
            });
        } else {
            try {
                String fileContents = FileUtils.readFileToString(fileToLoad, Charset.defaultCharset());
                disprovingProject = DisprovingProject.generateFromSavestring(fileContents);
                success = true;
            } catch (Exception ex) {
                success = false;
                ErrorLogger.logError("Error while trying to load DISPRO from file.",
                                     ErrorLogger.ErrorTypes.ERROR_LOADING_DISPRO);
            }
            Platform.runLater(() -> {
                actionLogger.endProgress();
                uiThreadDisproCallback.accept(disprovingProject, success);
            });
        }
    }

}
