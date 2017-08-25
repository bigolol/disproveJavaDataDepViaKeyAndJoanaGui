/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui.asynctaskhandler;

import ch.qos.logback.core.util.FileUtil;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.GraphIntegrity;
import disproveviakeyandjoanagui.CurrentActionLogger;
import disproveviakeyandjoanagui.ErrorLogger;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;
import joanakeyrefactoring.CombinedApproach;
import joanakeyrefactoring.JoanaAndKeyCheckData;
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
    private boolean succes;
    private JoanaAndKeyCheckData currentCheckData;
    private BiConsumer<JoanaAndKeyCheckData, Boolean> uiThreadJoakCallback;
    private DisprovingProject disprovingProject;
    private BiConsumer<DisprovingProject, Boolean> uiThreadDisproCallback;

    private enum LoadOptions {
        JOAK, DISPRO
    }

    public AsyncBackgroundLoader(CurrentActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void loadJoakFile(File f, BiConsumer<JoanaAndKeyCheckData, Boolean> callback) {
        this.fileToLoad = f;
        this.uiThreadJoakCallback = callback;
        loadOption = loadOption.JOAK;
        actionLogger.startProgress("now parsing the joak file, this might take a while to build the sdg ...");
        new Thread(this).start();
    }

    public void loadDisproFie(File f, BiConsumer<DisprovingProject, Boolean> callback) {
        this.fileToLoad = f;
        this.uiThreadDisproCallback = callback;
        loadOption = loadOption.DISPRO;
        actionLogger.startProgress("now parsing the dispro file, this might take a while ...");
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (loadOption == LoadOptions.JOAK) {
            try {
                currentCheckData = CombinedApproach.parseInputFile(fileToLoad);
                succes = true;
            } catch (Exception ex) {
                succes = false;
                ErrorLogger.logError("CombinedApproach threw while trying to pass the joak file " + fileToLoad.getName(),
                        ErrorLogger.ErrorTypes.ERROR_PARSING_JOAK);
            }
            Platform.runLater(() -> {
                actionLogger.endProgress();
                uiThreadJoakCallback.accept(currentCheckData, succes);
            });
        } else {
            try {
                String fileContents = FileUtils.readFileToString(fileToLoad, Charset.defaultCharset());
                disprovingProject = DisprovingProject.generateFromSavestring(fileContents);
                succes = true;
            } catch (Exception ex) {
                succes = false;
                ErrorLogger.logError("error when trying to load dispro from file", ErrorLogger.ErrorTypes.ERROR_LOADING_DISPRO);
            }
            Platform.runLater(() -> {
                actionLogger.endProgress();
                uiThreadDisproCallback.accept(disprovingProject, succes);
            });
        }
    }

}
