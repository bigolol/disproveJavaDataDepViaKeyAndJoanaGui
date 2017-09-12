/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeygui;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import joanakeygui.joanahandler.JoanaInstance;

/**
 * FXML Controller class
 *
 * @author holger
 */
public class AddSourceDialogController implements Initializable {

    @FXML
    private ComboBox<String> selectMethodCB;
    @FXML
    private ComboBox<String> selectionCB;

    @FXML
    public void onOk() {
        stage.close();
    }

    @FXML
    public void onCancel() {
        selectionCB.setValue(null);
        stage.close();
    }

    private Stage stage;
    private JoanaInstance joanaInstance;

    final static String programPart = "Program Part";
    final static String callsToMethod = "Calls to Method";

    private String[] addingMethods = {programPart, callsToMethod};

    public void setJoanaInstance(JoanaInstance joanaInstance) {
        this.joanaInstance = joanaInstance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectMethodCB.getItems().clear();
        selectMethodCB.getItems().addAll(addingMethods);
        selectMethodCB.valueProperty().addListener((observable) -> {
            selectionCB.getItems().clear();
            String selected = selectMethodCB.getSelectionModel().getSelectedItem();
            if (selected != null && selected.equals(programPart)) {
                selectionCB.getItems().addAll(joanaInstance.getAllProgramPartsString());
            } else if (selected != null && selected.equals(callsToMethod)) {
                selectionCB.getItems().addAll(joanaInstance.getAllMethodsString());
            }
        });
    }

    public Optional<SinkOrSource> showForSink() {
        stage.showAndWait();
        String selectMethodStr = selectMethodCB.getSelectionModel().getSelectedItem();
        String selectionStr = selectionCB.getSelectionModel().getSelectedItem();
        if (selectionStr != null && selectMethodStr.equals(programPart)) {
            return Optional.of(SinkOrSource.createProgramPart(selectionStr, "low"));
        } else if (selectionStr != null && selectMethodStr.equals(callsToMethod)) {
            return Optional.of(SinkOrSource.createMethod(selectionStr, "low"));
        } else {
            return Optional.empty();
        }
    }

    public Optional<SinkOrSource> showForSrc() {
        stage.showAndWait();
        String selectMethodStr = selectMethodCB.getSelectionModel().getSelectedItem();
        String selectionStr = selectionCB.getSelectionModel().getSelectedItem();
        if (selectionStr != null && selectMethodStr.equals(programPart)) {
            return Optional.of(SinkOrSource.createProgramPart(selectionStr, "high"));
        } else if (selectionStr != null && selectMethodStr.equals(callsToMethod)) {
            return Optional.of(SinkOrSource.createMethod(selectionStr, "high"));
        } else {
            return Optional.empty();
        }
    }

}
