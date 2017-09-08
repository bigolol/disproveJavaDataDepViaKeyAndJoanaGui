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
import java.util.Collection;
import joanakeyrefactoring.JoanaAndKeyCheckData;
import joanakeyrefactoring.loopinvarianthandling.LoopInvPosAndMethBodExtracter;
import joanakeyrefactoring.StateSaver;
import joanakeyrefactoring.SummaryEdgeAndMethodToCorresData;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.javaforkeycreator.JavaForKeyCreator;
import joanakeyrefactoring.staticCG.JCallGraph;
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
    private SummaryEdgeAndMethodToCorresData summaryEdgeToCorresData;
    private LoopInvPosAndMethBodExtracter loopInvPosAndMethodBodyExtracter;

    private DisprovingProject() {
    }

    public String getPathToSDG() {
        return pathToSDG;
    }

    public String getPathToJar() {
        return pathToJar;
    }

    public String getPathToJava() {
        return pathToJava;
    }

    public SDG getSdg() {
        return sdg;
    }

    public StateSaver getStateSaver() {
        return stateSaver;
    }

    public JCallGraph getCallGraph() {
        return callGraph;
    }

    public ViolationsWrapper getViolationsWrapper() {
        return violationsWrapper;
    }

    public String getProjName() {
        return pathToJar.substring(pathToJar.lastIndexOf("/") + 1, pathToJar.length() - ".jar".length());
    }

    public void saveSDG() throws FileNotFoundException, IOException {
        File f = File.createTempFile(getProjName(), ".pdg", new File("savedata"));
        if (f.exists()) {
            f.delete();
        }
        f.getParentFile().mkdirs();
        PrintWriter out = new PrintWriter(f);
        SDGSerializer.toPDGFormat(sdg, out);
        pathToSDG = "savedata/" + f.getName();
    }

    public String generateSaveString() {
        StringBuilder created = new StringBuilder();
        created.append("{");
        JsonHelper.addJsonStringToStringBuilder(created, "path_to_jar", pathToJar);
        created.append(",\n");
        JsonHelper.addJsonStringToStringBuilder(created, "path_to_java_source", pathToJava);
        created.append(",\n");
        JsonHelper.addJsonStringToStringBuilder(created, "path_to_sdg", pathToSDG);
        created.append(",\n");
        JsonHelper.addKeyValueToJsonStringbuilder(created, "state_saver", stateSaver.getSaveString());
        created.append(",\n");
        JsonHelper.addKeyValueToJsonStringbuilder(created, "edges_methods_to_values",
                summaryEdgeToCorresData.generateSaveString(violationsWrapper));
        created.append(",\n");
        JsonHelper.addKeyValueToJsonStringbuilder(created, "violation_wrapper",
                violationsWrapper.generateSaveString());

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
        disprovingProject.sdg = SDGProgram.loadSDG(pathToSdg).getSDG();
        disprovingProject.callGraph = new JCallGraph();
        disprovingProject.callGraph.generateCG(new File(pathToJar));
        disprovingProject.stateSaver =
                StateSaver.generateFromJson(statesaveJsonObj, disprovingProject.sdg);
        disprovingProject.violationsWrapper = ViolationsWrapper.generateFromJsonObj(
                violWrapperJsonObj, disprovingProject.sdg, disprovingProject.callGraph);
        disprovingProject.loopInvPosAndMethodBodyExtracter = new LoopInvPosAndMethBodExtracter();
        disprovingProject.loopInvPosAndMethodBodyExtracter.findAllLoopPositionsAndMethodBodies(
                disprovingProject.violationsWrapper.getSummaryEdgesAndCorresJavaMethods().values(),
                disprovingProject.pathToJava);
        disprovingProject.summaryEdgeToCorresData
                = SummaryEdgeAndMethodToCorresData.generateFromjson(
                        jSONObject.getJSONObject("edges_methods_to_values"),
                        disprovingProject.callGraph, disprovingProject.sdg);
        disprovingProject.violationsWrapper.addListener(disprovingProject.summaryEdgeToCorresData);
        return disprovingProject;
    }

    public static DisprovingProject generateFromCheckdata(JoanaAndKeyCheckData checkData)
            throws IOException {
        DisprovingProject disprovingProject = new DisprovingProject();
        disprovingProject.pathToJar = checkData.getPathToJar();
        disprovingProject.pathToJava = checkData.getPathToJavaFile();
        disprovingProject.stateSaver = checkData.getStateSaver();
        disprovingProject.sdg = checkData.getAnalysis().getProgram().getSDG();
        disprovingProject.callGraph = new JCallGraph();
        disprovingProject.callGraph.generateCG(new File(disprovingProject.pathToJar));
        disprovingProject.stateSaver.generatePersistenseStructures(disprovingProject.sdg);

        checkData.addAnnotations();
        Collection<? extends IViolation<SecurityNode>> viols = checkData.getAnalysis().doIFC();
        disprovingProject.violationsWrapper
                = new ViolationsWrapper(
                        viols, disprovingProject.sdg, checkData.getAnalysis(),
                        disprovingProject.callGraph);

        disprovingProject.loopInvPosAndMethodBodyExtracter = new LoopInvPosAndMethBodExtracter();
        disprovingProject.loopInvPosAndMethodBodyExtracter.findAllLoopPositionsAndMethodBodies(
                disprovingProject.violationsWrapper.getSummaryEdgesAndCorresJavaMethods().values(),
                disprovingProject.pathToJava);

        disprovingProject.summaryEdgeToCorresData = new SummaryEdgeAndMethodToCorresData(
                disprovingProject.violationsWrapper.getSummaryEdgesAndCorresJavaMethods(),
                disprovingProject.sdg,
                new JavaForKeyCreator(checkData.getPathToJavaFile(),
                        disprovingProject.callGraph,
                        disprovingProject.sdg,
                        disprovingProject.stateSaver));

        disprovingProject.violationsWrapper.addListener(disprovingProject.summaryEdgeToCorresData);

        return disprovingProject;
    }

    public SummaryEdgeAndMethodToCorresData getSummaryEdgeToCorresData() {
        return summaryEdgeToCorresData;
    }

}
