/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.persistence;

import edu.kit.joana.api.sdg.SDGProgram;
import edu.kit.joana.ifc.sdg.core.SecurityNode;
import edu.kit.joana.ifc.sdg.core.violations.IViolation;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGSerializer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import joanakeyrefactoring.JoanaAndKeyCheckData;
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
    
    public void saveSDG() throws FileNotFoundException {
        String saveStr = SDGSerializer.toPDGFormat(sdg);
        String javaProjName = pathToJar.substring(pathToJar.lastIndexOf("/") + 1, pathToJar.length() - ".jar".length());
        String saveFilePos = "savedata/" + javaProjName + "/sdg.pdg";
        File f = new File(saveFilePos);
        if(f.exists()) {
            f.delete();
        }
        f.getParentFile().mkdirs();
        PrintWriter out = new PrintWriter(f);
        out.write(saveStr);
        out.close();
        pathToSDG = saveFilePos;
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
        addKeyValueToJsonStringbuilder(created, "state_saver", stateSaver.getSaveString());
        created.append(",\n");
        addKeyValueToJsonStringbuilder(created, "violation_wrapper", 
                violationsWrapper.generateSaveString());
        created.append("}");
        System.out.println(created.toString());
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
        disprovingProject.sdg = SDGProgram.loadSDG(pathToSdg).getSDG();
        disprovingProject.callGraph = new JCallGraph();
        disprovingProject.callGraph.generateCG(new File(pathToJar));
        disprovingProject.stateSaver = StateSaver.generateFromJson(statesaveJsonObj);
        disprovingProject.violationsWrapper = ViolationsWrapper.generateFromJsonObj(
                violWrapperJsonObj, disprovingProject.sdg, disprovingProject.callGraph);
        return disprovingProject;
    }

    public static DisprovingProject generateFromCheckdata(JoanaAndKeyCheckData checkData) throws IOException {
        DisprovingProject disprovingProject = new DisprovingProject();
        disprovingProject.pathToJar = checkData.getPathToJar();
        disprovingProject.pathToJava = checkData.getPathToJavaFile();
        disprovingProject.stateSaver = checkData.getStateSaver();
        disprovingProject.sdg = checkData.getAnalysis().getProgram().getSDG();
        disprovingProject.callGraph = new JCallGraph();
        disprovingProject.callGraph.generateCG(new File(disprovingProject.pathToJar));
        Collection<? extends IViolation<SecurityNode>> viols = checkData.getAnalysis().doIFC();
        disprovingProject.violationsWrapper = 
                new ViolationsWrapper(
                        viols, disprovingProject.sdg, checkData.getAnalysis(), 
                        disprovingProject.callGraph);
        return disprovingProject;
    }

}
