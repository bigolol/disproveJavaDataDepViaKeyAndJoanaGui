/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author holger
 */
public class DisproveViaKeYAndJoanaGUI extends Application {

    private static final String FXML_MAIN_RESOURCE = "MainWindowFXMLDoc.fxml";

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML_MAIN_RESOURCE));
        Parent root = fxmlLoader.load();
        MainWindowController controller = (MainWindowController) fxmlLoader.getController();

        //setup controller here
        controller.setMainStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        //I do this so the menus opens at the correct place. For some reason it doesnt sometimes
        //if the main stage isnt moved by a bit
        stage.setX(stage.getX() + 1);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
