/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeygui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author holgerklein
 */
public class JoanaKeyGui extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));

        Parent root = fXMLLoader.load();
        FXMLDocumentController controller = (FXMLDocumentController) fXMLLoader.getController();
        controller.setStage(stage);


        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
