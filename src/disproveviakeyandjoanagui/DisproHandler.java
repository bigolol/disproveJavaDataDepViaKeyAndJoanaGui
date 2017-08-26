/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import disproveviakeyandjoanagui.asynctaskhandler.AsyncBackgroundDisproCreator;
import edu.kit.joana.ifc.sdg.core.SecurityNode;
import edu.kit.joana.ifc.sdg.core.violations.IViolation;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import joanakeyrefactoring.JoanaAndKeyCheckData;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.javaforkeycreator.LoopInvariantGenerator;
import joanakeyrefactoring.persistence.DisprovingProject;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.fxmisc.richtext.CodeArea;
import org.reactfx.util.TriConsumer;

/**
 *
 * @author holger
 */
public class DisproHandler {

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
    private Button buttonCalcKeYContract;

    private ListView<String> listViewSummaryEdges;
    private ListView<String> listViewUncheckedChops;
    private ListView<String> listViewCalledMethodsInSE;
    private ListView<String> listViewLoopsInSE;
    private ListView<String> listViewFormalInoutPairs;

    private AnchorPane anchorPaneMethodCode;
    private AnchorPane anchorPaneLoopInvariant;
    private AnchorPane anchorPaneKeyContract;

    private Map<Integer, SDGEdge> itemIndexToSummaryEdge = new HashMap<>();
    private StaticCGJavaMethod currentSelectedMethod;

    private HashMap<Integer, SDGNodeTuple> currentIndexToNodeTuple = new HashMap<>();
    private Map<SDGEdge, StaticCGJavaMethod> summaryEdgesAndCorresJavaMethods;

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
            Button buttonCalcKeYContract) {
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
        this.anchorPaneMethodCode = anchorPaneMethodCode;
        this.anchorPaneLoopInvariant = anchorPaneLoopInvariant;
        this.anchorPaneKeyContract = anchorPaneKeyContract;
        this.buttonResetLoopInvariant = buttonResetLoopInvariant;
        this.buttonSaveLoopInvariant = buttonSaveLoopInvariant;
        this.buttonCalcKeYContract = buttonCalcKeYContract;

        JavaCodeEditor javaCodeEditor = new JavaCodeEditor();

        methodCodeArea = javaCodeEditor.getCodeArea();
        addCodeAreaToAnchorPane(methodCodeArea, anchorPaneMethodCode);
        methodCodeArea.setDisable(true);

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
        listViewLoopsInSE.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            onLoopInvSelectionChanged(newValue);
        });
    }

    private void addCodeAreaToAnchorPane(CodeArea codeArea, AnchorPane anchorPane) {
        anchorPane.getChildren().add(codeArea);
        anchorPane.setTopAnchor(codeArea, 0.0);
        anchorPane.setRightAnchor(codeArea, 0.0);
        anchorPane.setBottomAnchor(codeArea, 0.0);
        anchorPane.setLeftAnchor(codeArea, 0.0);
    }

    public DisprovingProject getDisprovingProject() {
        return disprovingProject;
    }

    public void handleNewDispro(JoanaAndKeyCheckData checkData) {
        backgroundDisproCreator.generateFromCheckData(checkData, (dispro, worked) -> {
            if (worked) {
                disprovingProject = dispro;
                handleNewDisproSet();
            }
        });
        mainMenu.setDisable(false);
    }

    public void handleNewDispro(DisprovingProject disprovingProject) {
        this.disprovingProject = disprovingProject;
        handleNewDisproSet();
        mainMenu.setDisable(false);
    }

    private void setLoopInvInCurrent(int pos, String inv) {
        currentSelectedMethod.setLoopInvariant(pos, inv);
    }

    private void resetListView(ListView<String> listView) {
        try {
            listView.getSelectionModel().clearSelection();
            listView.getItems().clear();
        } catch (Exception e) {
        }
    }

    private void clearCodeAreaForNewCode(CodeArea codeArea, String newCode) {
        try {
            codeArea.clear();
            codeArea.replaceText(0, 0, newCode);
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
        SDGEdge e = itemIndexToSummaryEdge.get(newValue);

        currentSelectedMethod = summaryEdgesAndCorresJavaMethods.get(e);

        //handle the code areas
        String methodBody = currentSelectedMethod.getMethodBody();

        clearCodeAreaForNewCode(loopInvariantCodeArea, "");
        clearCodeAreaForNewCode(keyContractCodeArea, "");
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

        Collection<SDGNodeTuple> allFormalPairs = disprovingProject.getSdg().getAllFormalPairs(e.getSource(), e.getTarget());
        currentIndexToNodeTuple.clear();
        int index = 0;
        for (SDGNodeTuple t : allFormalPairs) {
            listViewFormalInoutPairs.getItems().add(t.toString());
            currentIndexToNodeTuple.put(index++, t);
        }
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
    private void onLoopInvSelectionChanged(String newValue) {
        if (newValue == null) {
            return;
        }
        int relPos = Integer.valueOf(newValue);
        clearCodeAreaForNewCode(loopInvariantCodeArea, currentSelectedMethod.getLoopInvariant(relPos));

        buttonResetLoopInvariant.setOnAction((ActionEvent event) -> {
            String template = LoopInvariantGenerator.getTemplate();
            setLoopInvInCurrent(relPos, template);
            clearCodeAreaForNewCode(loopInvariantCodeArea, template);
        });
        buttonSaveLoopInvariant.setOnAction((event) -> {
            setLoopInvInCurrent(relPos, loopInvariantCodeArea.getText());
        });
    }

    /**
     * this gets called whenever a new .dispro file is loaded or a new
     * DisprovingProject is created from a .joak file.
     */
    private void handleNewDisproSet() {
        joanaKeyInterfacer = new JoanaKeyInterfacer(
                violationsWrapper,
                disprovingProject.getPathToJava(),
                disprovingProject.getCallGraph(),
                disprovingProject.getSdg(),
                disprovingProject.getStateSaver());
        //
        //do view stuff
        //
        labelProjName.setText(disprovingProject.getProjName());
        violationsWrapper = disprovingProject.getViolationsWrapper();

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
}
