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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

/**
 *
 * @author holger
 */
public class DisproHandler {

    private AsyncBackgroundDisproCreator backgroundDisproCreator;
    private DisprovingProject disprovingProject;
    private ViolationsWrapper violationsWrapper;

    private CodeArea methodCodeArea;
    private CodeArea loopInvariantCodeArea;

    private Label labelProjName;
    private Label labelSummaryEdge;
    private Label labelSomeOtherData;
    private MenuBar mainMenu;

    private Button buttonSaveLoopInvariant;
    private Button buttonResetLoopInvariant;

    private ListView<String> listViewSummaryEdges;
    private ListView<String> listViewUncheckedChops;
    private ListView<String> listViewCalledMethodsInSE;
    private ListView<String> listViewLoopsInSE;

    private AnchorPane anchorPaneMethodCode;
    private AnchorPane anchorPaneLoopInvariant;

    private Map<Integer, SDGEdge> itemIndexToSummaryEdge = new HashMap<>();
    private StaticCGJavaMethod currentSelectedMethod;

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
            AnchorPane anchorPaneMethodCode,
            AnchorPane anchorPaneLoopInvariant,
            Button buttonSaveLoopInvariant,
            Button buttonResetLoopInvariant) {
        backgroundDisproCreator = new AsyncBackgroundDisproCreator(currentActionLogger);
        this.labelProjName = labelProjName;
        this.labelSummaryEdge = labelSummaryEdge;
        this.labelSomeOtherData = labelSomeOtherData;
        this.mainMenu = mainMenu;
        this.listViewUncheckedChops = listViewUncheckedChops;
        this.listViewSummaryEdges = listViewUncheckedEdges;
        this.listViewCalledMethodsInSE = listViewCalledMethodsInSE;
        this.listViewLoopsInSE = listViewLoopsInSE;
        this.anchorPaneMethodCode = anchorPaneMethodCode;
        this.anchorPaneLoopInvariant = anchorPaneLoopInvariant;
        this.buttonResetLoopInvariant = buttonResetLoopInvariant;
        this.buttonSaveLoopInvariant = buttonSaveLoopInvariant;

        JavaCodeEditor javaCodeEditor = new JavaCodeEditor();
        methodCodeArea = javaCodeEditor.getCodeArea();        
        addCodeAreaToAnchorPane(methodCodeArea, anchorPaneMethodCode);
        methodCodeArea.setDisable(true);
        
        loopInvariantCodeArea = javaCodeEditor.getCodeArea();
        addCodeAreaToAnchorPane(loopInvariantCodeArea, this.anchorPaneLoopInvariant);

        labelProjName.setText("");
        labelSummaryEdge.setText("");
        labelSomeOtherData.setText("");
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

    private void handleNewDisproSet() {
        labelProjName.setText(disprovingProject.getProjName());
        violationsWrapper = disprovingProject.getViolationsWrapper();

        listViewSummaryEdges.getItems().clear();
        listViewUncheckedChops.getItems().clear();
        listViewCalledMethodsInSE.getItems().clear();
        listViewLoopsInSE.getItems().clear();

        itemIndexToSummaryEdge = new HashMap<>();

        Collection<? extends IViolation<SecurityNode>> uncheckedViolations = violationsWrapper.getUncheckedViolations();
        for (IViolation<SecurityNode> v : uncheckedViolations) {
            listViewUncheckedChops.getItems().add(v.toString());
        }

        Map<SDGEdge, StaticCGJavaMethod> summaryEdgesAndCorresJavaMethods = violationsWrapper.getSummaryEdgesAndCorresJavaMethods();

        int i = 0;
        for (SDGEdge e : summaryEdgesAndCorresJavaMethods.keySet()) {
            listViewSummaryEdges.getItems().add(e.toString() + " "
                    + summaryEdgesAndCorresJavaMethods.get(e).toString());
            itemIndexToSummaryEdge.put(i++, e);
        }

        listViewSummaryEdges.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            SDGEdge e = itemIndexToSummaryEdge.get(newValue);
            listViewCalledMethodsInSE.getItems().clear();
            listViewLoopsInSE.getItems().clear();

            currentSelectedMethod = summaryEdgesAndCorresJavaMethods.get(e);
            String methodBody = currentSelectedMethod.getMethodBody();

            methodCodeArea.replaceText(0, methodCodeArea.getText().length(), methodBody);

            for (int relPos : currentSelectedMethod.getRelPosOfLoops()) {
                listViewLoopsInSE.getItems().add(String.valueOf(relPos));
            }

            for (StaticCGJavaMethod m : currentSelectedMethod.getCalledFunctionsRec()) {
                listViewCalledMethodsInSE.getItems().add(m.toString());
            }

            loopInvariantCodeArea.replaceText(
                    0,
                    loopInvariantCodeArea.getText().length(),
                    "");
        });
        
        listViewLoopsInSE.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> o, String ov, String nv) -> {
            if (nv == null) {
                return;
            }
            int relPos = Integer.valueOf(nv);
            loopInvariantCodeArea.replaceText(
                    0,
                    loopInvariantCodeArea.getText().length(),
                    currentSelectedMethod.getLoopInvariant(relPos));
            buttonResetLoopInvariant.setOnAction((ActionEvent event) -> {
                String template = LoopInvariantGenerator.getTemplate();
                setLoopInvInCurrent(relPos, template);
                loopInvariantCodeArea.replaceText(0, loopInvariantCodeArea.getText().length(), template);
            });
            buttonSaveLoopInvariant.setOnAction((event) -> {
                setLoopInvInCurrent(relPos, loopInvariantCodeArea.getText());
            });
        });

    }
}
