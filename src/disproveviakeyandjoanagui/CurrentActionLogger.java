/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

/**
 *
 * @author holger
 */
public class CurrentActionLogger {

    private Label labelCurrentAction;
    private ProgressIndicator progressIndicator;

    public CurrentActionLogger(Label labelCurrentAction, ProgressIndicator progressIndicator) {
        this.labelCurrentAction = labelCurrentAction;
        this.progressIndicator = progressIndicator;
        endProgress();
    }

    public void startProgress(String msg) {
        progressIndicator.setVisible(true);
        labelCurrentAction.setText(msg);
    }

    public void endProgress() {
        labelCurrentAction.setText("");
        progressIndicator.setVisible(false);
    }

}
