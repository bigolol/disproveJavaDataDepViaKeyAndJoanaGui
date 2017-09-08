/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import disproveviakeyandjoanagui.asynctaskhandler.AsyncAutoRunner;
import disproveviakeyandjoanagui.asynctaskhandler.AsyncBackgroundDisproCreator;
import edu.kit.joana.ifc.sdg.core.SecurityNode;
import edu.kit.joana.ifc.sdg.core.violations.IViolation;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import joanakeyrefactoring.AutomationHelper;
import joanakeyrefactoring.JoanaAndKeyCheckData;
import joanakeyrefactoring.ViolationChop;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.persistence.DisprovingProject;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.fxmisc.richtext.CodeArea;

/**
 *
 * @author holger
 */
public class DisproHandler implements ViolationsWrapperListener {

    private AsyncBackgroundDisproCreator backgroundDisproCreator;
    private DisprovingProject disprovingProject;
    private ViolationsWrapper violationsWrapper;
    private JoanaKeyInterfacer joanaKeyInterfacer;

    private CodeArea methodCodeArea;
    private CodeArea loopInvariantCodeArea;
    private CodeArea keyContractCodeArea;

    private Label labelProjName;
    private Label labelSummaryEdge;
    private Label labelSomeOtherData;
    private MenuBar mainMenu;

    private Button buttonSaveLoopInvariant;
    private Button buttonResetLoopInvariant;
    private Button buttonMarkAsDisproved;
    private Button buttonOpenSelected;
    private Button buttonTryDisprove;
    private Button buttonRunAuto;

    private ListView<String> listViewSummaryEdges;
    private ListView<String> listViewUncheckedChops;
    private ListView<String> listViewCalledMethodsInSE;
    private ListView<String> listViewLoopsInSE;
    private ListView<String> listViewFormalInoutPairs;

    //private AnchorPane anchorPaneMethodCode;
    private AnchorPane anchorPaneLoopInvariant;
    private AnchorPane anchorPaneKeyContract;

    private Map<Integer, SDGEdge> itemIndexToSummaryEdge = new HashMap<>();
    private HashMap<Integer, SDGNodeTuple> currentIndexToNodeTuple = new HashMap<>();
    private Map<SDGEdge, StaticCGJavaMethod> summaryEdgesAndCorresJavaMethods;

    private SDGEdge currentSelectedEdge;
    private StaticCGJavaMethod currentSelectedMethod;
    private CurrentActionLogger currentActionLogger;

    public DisproHandler(
            CurrentActionLogger currentActionLogger,
            Label labelProjName,
            Label labelSummaryEdge,
            Label labelSomeOtherData,
            MenuBar mainMenu,
            ListView<String> listViewUncheckedEdges,
            ListView<String> listViewUncheckedChops,
            ListView<String> listViewCalledMethodsInSE,
            ListView<String> listViewLoopsInSE,
            ListView<String> listViewFormalInoutPairs,
            AnchorPane anchorPaneMethodCode,
            AnchorPane anchorPaneLoopInvariant,
            AnchorPane anchorPaneKeyContract,
            Button buttonSaveLoopInvariant,
            Button buttonResetLoopInvariant,
            Button buttonMarkAsDisproved,
            Button buttonOpenSelected,
            Button buttonTryDisprove,
            Button buttonRunAtuo) {
        backgroundDisproCreator = new AsyncBackgroundDisproCreator(currentActionLogger);
        this.labelProjName = labelProjName;
        this.labelSummaryEdge = labelSummaryEdge;
        this.labelSomeOtherData = labelSomeOtherData;
        this.mainMenu = mainMenu;
        this.listViewUncheckedChops = listViewUncheckedChops;
        this.listViewSummaryEdges = listViewUncheckedEdges;
        this.listViewCalledMethodsInSE = listViewCalledMethodsInSE;
        this.listViewLoopsInSE = listViewLoopsInSE;
        this.listViewFormalInoutPairs = listViewFormalInoutPairs;
        //this.anchorPaneMethodCode = anchorPaneMethodCode;
        this.anchorPaneLoopInvariant = anchorPaneLoopInvariant;
        this.anchorPaneKeyContract = anchorPaneKeyContract;
        this.buttonResetLoopInvariant = buttonResetLoopInvariant;
        this.buttonSaveLoopInvariant = buttonSaveLoopInvariant;
        this.buttonMarkAsDisproved = buttonMarkAsDisproved;
        this.buttonOpenSelected = buttonOpenSelected;
        this.buttonTryDisprove = buttonTryDisprove;
        this.buttonRunAuto = buttonRunAtuo;
        this.currentActionLogger = currentActionLogger;

        JavaCodeEditor javaCodeEditor = new JavaCodeEditor();

        methodCodeArea = javaCodeEditor.getCodeArea();
        addCodeAreaToAnchorPane(methodCodeArea, anchorPaneMethodCode);
        methodCodeArea.setDisable(false);

        keyContractCodeArea = javaCodeEditor.getCodeArea();
        addCodeAreaToAnchorPane(keyContractCodeArea, this.anchorPaneKeyContract);
        keyContractCodeArea.setDisable(true);

        loopInvariantCodeArea = javaCodeEditor.getCodeArea();
        addCodeAreaToAnchorPane(loopInvariantCodeArea, this.anchorPaneLoopInvariant);

        labelProjName.setText("");
        labelSummaryEdge.setText("");
        labelSomeOtherData.setText("");

        listViewSummaryEdges.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            onSummaryEdgeSelectionChange((int) newValue);
        });
        listViewFormalInoutPairs.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            onFormalPairSelectionChange((int) newValue);
        });
        listViewLoopsInSE.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            onLoopInvSelectionChanged((int) newValue);
        });
    }

    public void setAllButtonsDisable(boolean disable) {
        buttonRunAuto.setDisable(disable);
        buttonMarkAsDisproved.setDisable(disable);
        buttonResetLoopInvariant.setDisable(disable);
        buttonSaveLoopInvariant.setDisable(disable);
        buttonOpenSelected.setDisable(disable);
        buttonTryDisprove.setDisable(disable);
    }

    private void onPressRunAuto() {
        if (AsyncAutoRunner.keepRunning.get()) {
            AsyncAutoRunner.stop();
            buttonRunAuto.setText("start auto modus");
            buttonMarkAsDisproved.setDisable(false);
            buttonResetLoopInvariant.setDisable(false);
            buttonSaveLoopInvariant.setDisable(false);
            buttonOpenSelected.setDisable(false);
            buttonTryDisprove.setDisable(false);
            mainMenu.setDisable(false);
        } else {
            currentActionLogger.startProgress("running auto pilot");
            buttonRunAuto.setText("stop auto modus");
            buttonMarkAsDisproved.setDisable(true);
            buttonResetLoopInvariant.setDisable(true);
            buttonSaveLoopInvariant.setDisable(true);
            buttonOpenSelected.setDisable(true);
            buttonTryDisprove.setDisable(true);
            mainMenu.setDisable(true);
            AsyncAutoRunner.startAutoDisproving(
                    violationsWrapper,
                    joanaKeyInterfacer.getJavaForKeyCreator(),
                    joanaKeyInterfacer.getSummaryEdgeToCorresData(),
                    currentActionLogger,
                    listViewSummaryEdges);
        }
    }

    private void onPressMarkAsDisproved() {
        joanaKeyInterfacer.markAsDisproved(currentSelectedEdge);
    }

    private void onPressOpenInKey() {
        SDGNodeTuple formalNodeTuple = currentIndexToNodeTuple.get(0);
        try {
            joanaKeyInterfacer.openInKey(
                    currentSelectedEdge,
                    formalNodeTuple,
                    summaryEdgesAndCorresJavaMethods.get(currentSelectedEdge));
            labelSummaryEdge.setText(currentSelectedEdge.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void onPressTryDisprove() {
        SDGNodeTuple formalNodeTuple = currentIndexToNodeTuple.get(0);
        try {
            boolean worked = joanaKeyInterfacer.tryDisproveEdge(
                    currentSelectedEdge,
                    formalNodeTuple,
                    summaryEdgesAndCorresJavaMethods.get(currentSelectedEdge));
            labelSummaryEdge.setText(currentSelectedEdge.toString());
            if (worked) {
                labelSomeOtherData.setText("disproving worked");
                violationsWrapper.removeEdge(currentSelectedEdge);
            } else {
                labelSomeOtherData.setText("could not disprove edge");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLogger.logError("an error occured while trying to create the jave code to disprove",
                    ErrorLogger.ErrorTypes.ERROR_WRITING_FILE_TO_DISK);
        }
    }

    private void addCodeAreaToAnchorPane(CodeArea codeArea, AnchorPane anchorPane) {
        anchorPane.getChildren().add(codeArea);
        AnchorPane.setTopAnchor(codeArea, 0.0);
        AnchorPane.setRightAnchor(codeArea, 0.0);
        AnchorPane.setBottomAnchor(codeArea, 0.0);
        AnchorPane.setLeftAnchor(codeArea, 0.0);
    }

    public DisprovingProject getDisprovingProject() {
        return disprovingProject;
    }

    public void handleNewDispro(JoanaAndKeyCheckData checkData) {
        setAllButtonsDisable(true);
        backgroundDisproCreator.generateFromCheckData(checkData, (dispro, worked) -> {
            if (worked) {
                try {
                    disprovingProject = dispro;
                    handleNewDisproSet();
                } catch (Exception e) {
                    ErrorLogger.logError("asdasdasdasd", ErrorLogger.ErrorTypes.ERROR_PARSING_JOAK);
                }
            }
            setAllButtonsDisable(false);
            mainMenu.setDisable(false);
        });
    }

    public void handleNewDispro(DisprovingProject disprovingProject) {
        setAllButtonsDisable(true);
        try {
            this.disprovingProject = disprovingProject;
            handleNewDisproSet();
        } catch (Exception e) {
            ErrorLogger.logError("asdasdasdasd", ErrorLogger.ErrorTypes.ERROR_PARSING_JOAK);
        }
        mainMenu.setDisable(false);
        setAllButtonsDisable(false);
    }

    /*private void setLoopInvInCurrent(int pos, String inv) {
        joanaKeyInterfacer.setLoopInvariantFor(currentSelectedEdge, pos, inv);
    }*/

    private void resetListView(ListView<String> listView) {
        try {
            listView.getSelectionModel().clearSelection();
            listView.getItems().clear();
        } catch (Exception e) {
        }
    }

    private void clearCodeAreaForNewCode(CodeArea codeArea, String newCode) {
        try {
            codeArea.replaceText(0, codeArea.getLength(), newCode);
        } catch (Exception e) {
        }
    }

    //#######################################################################
    //this gets run whenever the selected SUMMARY EDGE changes-------------->
    //#######################################################################
    private void onSummaryEdgeSelectionChange(int newValue) {
        if (newValue < 0) {
            return;
        }
        currentSelectedEdge = itemIndexToSummaryEdge.get(newValue);

        currentSelectedMethod = summaryEdgesAndCorresJavaMethods.get(currentSelectedEdge);

        //handle the code areas
        String methodBody = currentSelectedMethod.getMethodBody();

        clearCodeAreaForNewCode(loopInvariantCodeArea, "");
        clearCodeAreaForNewCode(methodCodeArea, methodBody);

        //populate the other list views
        resetListView(listViewCalledMethodsInSE);
        resetListView(listViewLoopsInSE);
        resetListView(listViewFormalInoutPairs);

        for (int relPos : currentSelectedMethod.getRelPosOfLoops()) {
            listViewLoopsInSE.getItems().add(String.valueOf(relPos));
        }
        for (StaticCGJavaMethod m : currentSelectedMethod.getCalledFunctionsRec()) {
            listViewCalledMethodsInSE.getItems().add(m.toString());
        }

        Collection<SDGNodeTuple> allFormalPairs = disprovingProject.getSdg().getAllFormalPairs(
                currentSelectedEdge.getSource(), currentSelectedEdge.getTarget());
        currentIndexToNodeTuple.clear();
        int index = 0;
        for (SDGNodeTuple t : allFormalPairs) {
            listViewFormalInoutPairs.getItems().add(t.toString());
            currentIndexToNodeTuple.put(index++, t);
        }
        listViewFormalInoutPairs.getSelectionModel().select(0);
    }

    //#######################################################################
    //this gets run whenever the selected FORMAL NODE TUPLE changes-------------->
    //#######################################################################
    private void onFormalPairSelectionChange(int newValue) {
        if (newValue < 0) {
            return;
        }
        SDGNodeTuple nodeTuple = currentIndexToNodeTuple.get(newValue);
        clearCodeAreaForNewCode(
                keyContractCodeArea,
                joanaKeyInterfacer.getKeyContractFor(nodeTuple, currentSelectedMethod));
    }

    //#######################################################################
    //this gets run whenever the selected LOOP INVARIANT changes-------------->
    //#######################################################################
    private void onLoopInvSelectionChanged(int newValue) {
        if (newValue == -1) {
            return;
        }

        clearCodeAreaForNewCode(loopInvariantCodeArea, joanaKeyInterfacer.getLoopInvariantFor(currentSelectedEdge, newValue));

        buttonResetLoopInvariant.setOnAction((ActionEvent event) -> {
            joanaKeyInterfacer.resetLoopInvariant(currentSelectedEdge, newValue);
            clearCodeAreaForNewCode(loopInvariantCodeArea,
                    joanaKeyInterfacer.getLoopInvariantFor(currentSelectedEdge, newValue));
        });

        buttonSaveLoopInvariant.setOnAction((event) -> {
            joanaKeyInterfacer.setLoopInvariantFor(currentSelectedEdge, newValue, loopInvariantCodeArea.getText());
        });
    }

    /**
     * this gets called whenever a new .dispro file is loaded or a new
     * DisprovingProject is created from a .joak file.
     */
    private void handleNewDisproSet() throws IOException {
        violationsWrapper = disprovingProject.getViolationsWrapper();
        violationsWrapper.addListener(this);

        joanaKeyInterfacer = new JoanaKeyInterfacer(disprovingProject);
        resetViews();
        //
        //setup disproving buttons
        //
        buttonTryDisprove.setOnAction((event) -> {
            onPressTryDisprove();
        });
        buttonOpenSelected.setOnAction((event) -> {
            onPressOpenInKey();
        });
        buttonMarkAsDisproved.setOnAction((event) -> {
            onPressMarkAsDisproved();
        });
        buttonRunAuto.setOnAction((event) -> {
            onPressRunAuto();
        });
    }

    private void resetViews() {
        //
        //do view stuff
        //
        labelProjName.setText(disprovingProject.getProjName());

        resetListView(listViewSummaryEdges);
        resetListView(listViewUncheckedChops);
        resetListView(listViewCalledMethodsInSE);
        resetListView(listViewLoopsInSE);
        resetListView(listViewFormalInoutPairs);

        itemIndexToSummaryEdge = new HashMap<>();

        Collection<? extends IViolation<SecurityNode>> uncheckedViolations = violationsWrapper.getUncheckedViolations();
        for (IViolation<SecurityNode> v : uncheckedViolations) {
            listViewUncheckedChops.getItems().add(v.toString());
        }

        summaryEdgesAndCorresJavaMethods = violationsWrapper.getSummaryEdgesAndCorresJavaMethods();

        int i = 0;
        for (SDGEdge e : summaryEdgesAndCorresJavaMethods.keySet()) {
            listViewSummaryEdges.getItems().add(e.toString() + " "
                    + summaryEdgesAndCorresJavaMethods.get(e).toString());
            itemIndexToSummaryEdge.put(i++, e);
        }
    }

    @Override
    public void parsedChop(ViolationChop chop) {
    }

    @Override
    public void disprovedEdge(SDGEdge e) {
        Platform.runLater(() -> {
            labelSomeOtherData.setText("disproved summary edge " + e.toString());

            int selectedIndex = listViewSummaryEdges.getSelectionModel().getSelectedIndex();

            currentIndexToNodeTuple.remove(selectedIndex);
            itemIndexToSummaryEdge.remove(selectedIndex);

            for (int i = selectedIndex + 1; i < listViewSummaryEdges.getItems().size(); ++i) {
                SDGEdge currentEdge = itemIndexToSummaryEdge.remove(i);
                itemIndexToSummaryEdge.put(i - 1, currentEdge);
                SDGNodeTuple currentRemovedNodeTuple = currentIndexToNodeTuple.remove(i);
                currentIndexToNodeTuple.put(i - 1, currentRemovedNodeTuple);
            }

            clearCodeAreaForNewCode(methodCodeArea, "");
            clearCodeAreaForNewCode(loopInvariantCodeArea, "");
            clearCodeAreaForNewCode(keyContractCodeArea, "");

            resetListView(listViewLoopsInSE);
            resetListView(listViewFormalInoutPairs);
            resetListView(listViewCalledMethodsInSE);

            listViewSummaryEdges.getItems().removeIf((s) -> {
                return s.startsWith(e.toString());
            });
            listViewSummaryEdges.getSelectionModel().clearSelection();
            listViewSummaryEdges.getSelectionModel().select(0);
        });
    }

    @Override
    public void disprovedChop(ViolationChop chop) {
        Platform.runLater(() -> {
            labelSomeOtherData.setText("the chop " + chop.toString() + " was completely disproved");
        });
    }

    @Override
    public void disprovedAll() {
        Platform.runLater(() -> {
            labelSomeOtherData.setText("disproved the information flow! hourayyyy! Incredible!");
            AutomationHelper.playSound("Victory Sound Effect.wav");
        });
    }

    @Override
    public void addedNewEdges(Map<SDGEdge, StaticCGJavaMethod> edgesToMethods, List<SDGEdge> edgesSorted, SDG sdg) {
        Platform.runLater(() -> {
            resetViews();
        });
    }

}
