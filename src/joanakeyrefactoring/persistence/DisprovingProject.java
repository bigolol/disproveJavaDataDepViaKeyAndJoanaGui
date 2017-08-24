/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.persistence;

import edu.kit.joana.ifc.sdg.graph.SDG;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import joanakeyrefactoring.StateSaver;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.staticCG.JCallGraph;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

/**
 *
 * @author hklein
 */
public class DisprovingProject {

    private String pathToSDG;
    private String pathToJar;
    private String pathToJava;
    private StateSaver stateSaver;
    private JCallGraph callGraph;
    private ViolationsWrapper violationsWrapper;
    private SDG sdg;

    private DisprovingProject() {
    }

    public SDG getSdg() {
        return sdg;
    }

    public JCallGraph getCallGraph() {
        return callGraph;
    }

    public ViolationsWrapper getViolationsWrapper() {
        return violationsWrapper;
    }

    private void addJsonStringToStringBuilder(StringBuilder stringBuilder, String key, String value) {
        stringBuilder.append("\"" + key + "\" : " + "\"" + value + "\"");
    }

    private void addKeyValueToJsonStringbuilder(StringBuilder stringBuilder, String key, String value) {
        stringBuilder.append("\"" + key + "\" : " + value);
    }
    
    private void addJsonObjValueToStringBuiler(StringBuilder stringBuilder, String key, String value) {
        stringBuilder.append("\"" + key + "\" : {\n");
        stringBuilder.append(value);
        stringBuilder.append("}\n");
    }

    public String generateSaveString() {
        StringBuilder created = new StringBuilder();
        created.append("{");
        addJsonStringToStringBuilder(created, "path_to_jar", pathToJar);
        created.append(",\n");
        addJsonStringToStringBuilder(created, "path_to_java_source", pathToJava);
        created.append(",\n");
        addJsonStringToStringBuilder(created, "path_to_sdg", pathToSDG);
        created.append(",\n");
        addJsonObjValueToStringBuiler(created, "state_saver", stateSaver.getSaveString());
        created.append(",\n");
        addJsonObjValueToStringBuiler(created, "violation_wrapper", violationsWrapper.generateSaveString());
        created.append("}");
        return created.toString();
    }
    

    public static DisprovingProject generateFromSavestring(String s) throws IOException {
        DisprovingProject disprovingProject = new DisprovingProject();
        JSONObject jSONObject = new JSONObject(s);
        String pathToJava = jSONObject.getString("path_to_java_source");
        String pathToJar = jSONObject.getString("path_to_jar");
        String pathToSdg = jSONObject.getString("path_to_sdg");
        JSONObject statesaveJsonObj = jSONObject.getJSONObject("state_saver");
        JSONObject violWrapperJsonObj = jSONObject.getJSONObject("violation_wrapper");
        disprovingProject.pathToJava = pathToJava;
        disprovingProject.pathToSDG = pathToSdg;
        disprovingProject.pathToJar = pathToJar;
        disprovingProject.sdg = SDG.readFrom(FileUtils.readFileToString(new File(pathToSdg),  Charset.defaultCharset()));
        disprovingProject.callGraph = new JCallGraph();
        disprovingProject.callGraph.generateCG(new File(pathToJar));
        disprovingProject.stateSaver = StateSaver.generateFromJson(statesaveJsonObj);
        disprovingProject.violationsWrapper = ViolationsWrapper.generateFromJsonObj(
                violWrapperJsonObj, disprovingProject.sdg, disprovingProject.callGraph);
        return disprovingProject;
    }

}
