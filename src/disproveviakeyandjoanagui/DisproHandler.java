/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import disproveviakeyandjoanagui.asynctaskhandler.AsyncBackgroundDisproCreator;
import edu.kit.joana.ifc.sdg.core.SecurityNode;
import edu.kit.joana.ifc.sdg.core.violations.IViolation;
import java.util.Collection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import joanakeyrefactoring.JoanaAndKeyCheckData;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.persistence.DisprovingProject;

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

    private ListView<String> listViewUncheckedEdges;
    private ListView<String> listViewUncheckedChops;

    public DisproHandler(
            CurrentActionLogger currentActionLogger,
            Label labelProjName,
            Label labelSummaryEdge,
            Label labelSomeOtherData,
            MenuBar mainMenu,
            ListView<String> listViewUncheckedEdges,
            ListView<String> listViewUncheckedChops) {
        backgroundDisproCreator = new AsyncBackgroundDisproCreator(currentActionLogger);
        this.labelProjName = labelProjName;
        this.labelSummaryEdge = labelSummaryEdge;
        this.labelSomeOtherData = labelSomeOtherData;
        this.mainMenu = mainMenu;
        this.listViewUncheckedChops = listViewUncheckedChops;
        this.listViewUncheckedEdges = listViewUncheckedEdges;
        
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
        
        Collection<? extends IViolation<SecurityNode>> uncheckedViolations = violationsWrapper.getUncheckedViolations();
        for(IViolation<SecurityNode> v : uncheckedViolations) {
            listViewUncheckedChops.getItems().add(v.toString());
        }       
    }
}
