/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeygui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import joanakeygui.joanahandler.JoanaInstance;

/**
 *
 * @author holger
 */
public class SourceSinkAdderDialogHandler {

    private ListView<String> sourcesList;
    private ListView<String> sinkList;
    private List<SinkOrSource> sources = new ArrayList<>();
    private List<SinkOrSource> sinks = new ArrayList<>();
    private JoanaInstance joanaInstance;
    AddSourceDialogController controller;

    public SourceSinkAdderDialogHandler(ListView<String> sourcesList, ListView<String> sinkList) throws IOException {
        this.sourcesList = sourcesList;
        this.sinkList = sinkList;
        FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource("AddSourceDialog.fxml"));

        Parent root = fXMLLoader.load();
        controller = (AddSourceDialogController) fXMLLoader.getController();

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        controller.setStage(stage);
    }

    public SinkOrSource letUserAddSink(Stage parentStage) {
        Optional<SinkOrSource> sink = controller.showForSink();
        sinks.add(sink.get());
        return sink.get();
    }

    public SinkOrSource letUserAddSrc(Stage parentStage) {
        Optional<SinkOrSource> src = controller.showForSrc();
        sources.add(src.get());
        return src.get();
    }

    public void removeSelectedSrc() {

    }

    public void removeSelectedSink() {

    }

    void setJoanaInstance(JoanaInstance joanaInstance) {
        this.joanaInstance = joanaInstance;
        controller.setJoanaInstance(joanaInstance);
    }

    public String createSinkSourceJson() {
        String sourcesStringTemplate = "sources : [SOURCES]";
        String sourcesJsonString = "{";
        for (SinkOrSource src : sources) {
            sourcesJsonString += src.generateJson();
            sourcesJsonString += "},\n{";
        }
        if (sources.isEmpty()) {
            sourcesJsonString = "";
        } else {
            sourcesJsonString = sourcesJsonString.substring(0, sourcesJsonString.length() - 3);
        }
        sourcesJsonString = sourcesStringTemplate.replace("SOURCES", sourcesJsonString);

        String sinkStringTemplate = "sinks : [SINKS]";
        String sinksJsonString = "{";
        for (SinkOrSource sink : sinks) {
            sinksJsonString += sink.generateJson();
            sinksJsonString += "},\n{";
        }
        if (sinks.isEmpty()) {
            sinksJsonString = "";
        } else {
            sinksJsonString = sinksJsonString.substring(0, sinksJsonString.length() - 3);
        }
        sinksJsonString = sinkStringTemplate.replace("SINKS", sinksJsonString);
        return sourcesJsonString + ",\n" + sinksJsonString;
    }

}
