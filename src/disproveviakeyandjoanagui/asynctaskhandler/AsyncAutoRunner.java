/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui.asynctaskhandler;

import disproveviakeyandjoanagui.CurrentActionLogger;
import disproveviakeyandjoanagui.JoanaKeYInterfacer;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import joanakeyrefactoring.AutomationHelper;
import joanakeyrefactoring.SummaryEdgeAndMethodToCorresData;
import joanakeyrefactoring.ViolationsDisproverSemantic;
import joanakeyrefactoring.ViolationsDisproverSemantic.PO_TYPE;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.javaforkeycreator.JavaForKeYCreator;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public class AsyncAutoRunner implements Runnable {

    public static AtomicBoolean keepRunning = new AtomicBoolean();
    private static ViolationsWrapper violationsWrapper;
    private static JavaForKeYCreator javaForKeyCreator;
    private static SummaryEdgeAndMethodToCorresData edgeAndMethodToCorresData;
    private static CurrentActionLogger actionLogger;
    private static ListView<String> summaryEdges;

    public static void startAutoDisproving(
            ViolationsWrapper violationsWrapper,
            JavaForKeYCreator javaForKeyCreator,
            SummaryEdgeAndMethodToCorresData edgeAndMethodToCorresData,
            CurrentActionLogger actionLogger,
            ListView<String> summaryEdges) {
        AsyncAutoRunner.violationsWrapper = violationsWrapper;
        AsyncAutoRunner.javaForKeyCreator = javaForKeyCreator;
        AsyncAutoRunner.edgeAndMethodToCorresData = edgeAndMethodToCorresData;
        AsyncAutoRunner.actionLogger = actionLogger;
        AsyncAutoRunner.summaryEdges = summaryEdges;
        
        keepRunning.set(true);
        new Thread(new AsyncAutoRunner()).start();
    }

    public static void stop() {
        keepRunning.set(false);
        Platform.runLater(() -> {
            actionLogger.startProgress("Please wait while the last disproving process finishes.");
        });
    }

    @Override
    public void run() {
        while (keepRunning.get()) {
            SDGEdge nextSummaryEdge = violationsWrapper.nextSummaryEdge();
            Platform.runLater(() -> {
                actionLogger.startProgress("Now generating files for " +
                                           nextSummaryEdge.toString() + ".");
                for (int i = 0; i < summaryEdges.getItems().size(); ++i) {
                    if (summaryEdges.getItems().get(i).startsWith(nextSummaryEdge.toString())) {
                        summaryEdges.getSelectionModel().select(i);
                        break;
                    }
                }
            });
            Collection<SDGNodeTuple> allFormalPairs =
                    violationsWrapper.getSdg().getAllFormalPairs(nextSummaryEdge.getSource(),
                                                                 nextSummaryEdge.getTarget());
            SDGNodeTuple firstFormalPair = allFormalPairs.iterator().next();
            StaticCGJavaMethod method = violationsWrapper.getMethodCorresToSummaryEdge(nextSummaryEdge);
            String contract = edgeAndMethodToCorresData.getContractFor(firstFormalPair);
            if (!keepRunning.get()) {
                break;
            }
            HashMap<StaticCGJavaMethod, String> methodToMostGeneralContract =
                    edgeAndMethodToCorresData.getMethodToMostGeneralContract();
            Platform.runLater(() -> {
                actionLogger.startProgress("Now trying to disprove " +
                                           nextSummaryEdge.toString() + ".");
            });
            if (!keepRunning.get()) {
                break;
            }
            try {
                //String pathToJave =
                javaForKeyCreator.generateJavaForFormalTupleCalledFromGui(
                        contract,
                        method,
                        edgeAndMethodToCorresData.getEdgeToLoopInvariantTemplate().get(nextSummaryEdge),
                        edgeAndMethodToCorresData.getEdgeToLoopInvariant().get(nextSummaryEdge),
                        methodToMostGeneralContract);
                if (!keepRunning.get()) {
                    break;
                }
                boolean worked =
                        AutomationHelper.runKeY(JoanaKeYInterfacer.PATH_TO_KeY,
                                                ViolationsDisproverSemantic.PO_PATH,
                                                PO_TYPE.INFORMATION_FLOW);
                if (!keepRunning.get()) {
                    break;
                }
                if (worked) {
                    worked =
                            AutomationHelper.runKeY(JoanaKeYInterfacer.PATH_TO_KeY,
                                                    ViolationsDisproverSemantic.PO_PATH,
                                                    PO_TYPE.FUNCTIONAL);
                }
                if (!keepRunning.get()) {
                    break;
                }
                if (worked) {
                    Platform.runLater(() -> {
                        actionLogger.startProgress(
                                "Succeeded disproving " +
                                nextSummaryEdge.toString() +
                                ", now removing from SDG ..."
                                );
                    });
                    violationsWrapper.removeEdge(nextSummaryEdge);
                } else {
                    Platform.runLater(() -> {
                        actionLogger.startProgress(
                                "Failed to disprove " +
                                nextSummaryEdge.toString() + ".");
                    });
                    violationsWrapper.checkedEdge(nextSummaryEdge);
                }
            } catch (IOException ex) {
                Logger.getLogger(AsyncAutoRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Platform.runLater(() -> {
            actionLogger.endProgress();
        });
    }

}
