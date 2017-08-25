/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import disproveviakeyandjoanagui.asynctaskhandler.AsyncBackgroundDisproCreator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import joanakeyrefactoring.JoanaAndKeyCheckData;
import joanakeyrefactoring.persistence.DisprovingProject;

/**
 *
 * @author holger
 */
public class DisproHandler {

    private AsyncBackgroundDisproCreator backgroundDisproCreator;
    private DisprovingProject disprovingProject;

    private Label labelProjName;
    private Label labelSummaryEdge;
    private Label labelSomeOtherData;
    private MenuBar mainMenu;

    public DisproHandler(
            CurrentActionLogger currentActionLogger,
            Label labelProjName,
            Label labelSummaryEdge,
            Label labelSomeOtherData,
            MenuBar mainMenu) {
        backgroundDisproCreator = new AsyncBackgroundDisproCreator(currentActionLogger);
        this.labelProjName = labelProjName;
        this.labelSummaryEdge = labelSummaryEdge;
        this.labelSomeOtherData = labelSomeOtherData;
        this.mainMenu = mainMenu;
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
    }
}
