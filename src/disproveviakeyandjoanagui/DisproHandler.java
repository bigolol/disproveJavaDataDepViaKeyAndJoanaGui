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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import joanakeyrefactoring.JoanaAndKeyCheckData;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.persistence.DisprovingProject;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public class DisproHandler {

    private AsyncBackgroundDisproCreator backgroundDisproCreator;
    private DisprovingProject disprovingProject;
    private ViolationsWrapper violationsWrapper;

    private Label labelProjName;
    private Label labelSummaryEdge;
    private Label labelSomeOtherData;
    private MenuBar mainMenu;

    private ListView<String> listViewSummaryEdges;
    private ListView<String> listViewUncheckedChops;
    private ListView<String> listViewCalledMethodsInSE;
    private ListView<String> listViewLoopsInSE;

    private Map<Integer, SDGEdge> itemIndexToSummaryEdge = new HashMap<>();
    
    public DisproHandler(
            CurrentActionLogger currentActionLogger,
            Label labelProjName,
            Label labelSummaryEdge,
            Label labelSomeOtherData,
            MenuBar mainMenu,
            ListView<String> listViewUncheckedEdges,
            ListView<String> listViewUncheckedChops,
            ListView<String> listViewCalledMethodsInSE,
            ListView<String> listViewLoopsInSE) {
        backgroundDisproCreator = new AsyncBackgroundDisproCreator(currentActionLogger);
        this.labelProjName = labelProjName;
        this.labelSummaryEdge = labelSummaryEdge;
        this.labelSomeOtherData = labelSomeOtherData;
        this.mainMenu = mainMenu;
        this.listViewUncheckedChops = listViewUncheckedChops;
        this.listViewSummaryEdges = listViewUncheckedEdges;
        this.listViewCalledMethodsInSE = listViewCalledMethodsInSE;
        this.listViewLoopsInSE = listViewLoopsInSE;

        labelProjName.setText("");
        labelSummaryEdge.setText("");
        labelSomeOtherData.setText("");
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
            listViewSummaryEdges.getItems().add(e.toString() + " " +
                    summaryEdgesAndCorresJavaMethods.get(e).toString());
            itemIndexToSummaryEdge.put(i++, e);
        }
        
        listViewSummaryEdges.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            SDGEdge e = itemIndexToSummaryEdge.get(newValue);
            listViewCalledMethodsInSE.getItems().clear();
            for(StaticCGJavaMethod m : summaryEdgesAndCorresJavaMethods.get(e).getCalledFunctionsRec()) {
                listViewCalledMethodsInSE.getItems().add(m.toString());
            }
        });
     
        
    }
}
