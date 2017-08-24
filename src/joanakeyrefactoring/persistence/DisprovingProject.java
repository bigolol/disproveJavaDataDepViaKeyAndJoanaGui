/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.persistence;

import edu.kit.joana.api.IFCAnalysis;
import edu.kit.joana.api.sdg.SDGProgram;
import edu.kit.joana.ifc.sdg.core.violations.ClassifiedViolation;
import edu.kit.joana.ifc.sdg.graph.SDG;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import joanakeyrefactoring.StateSaver;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.staticCG.JCallGraph;

/**
 *
 * @author hklein
 */
public class DisprovingProject {

    private String pathToSDG;
    private String pathToStateSaverJson;
    private IFCAnalysis ana;
    private Collection<ClassifiedViolation> classifiedViolations;
    private StateSaver stateSaver;
    private JCallGraph callGraph;
    private SDG sdg;
    private ViolationsWrapper violationsWrapper;
    private String pathToJar;
    private String pathToJava;

    private DisprovingProject() {
    }

    public DisprovingProject(
            String pathToSDG, String pathToStateSaverJson, String pathToViolations,
            String pathToSrc, String pathToJar, String pathToViolWrapper) throws IOException {
        this.pathToSDG = pathToSDG;
        this.pathToStateSaverJson = pathToStateSaverJson;
        SDGProgram program = SDGProgram.loadSDG(pathToSDG);
        sdg = program.getSDG();
        ana = new IFCAnalysis(program);
        stateSaver = StateSaver.generateFromSaveStr(pathToStateSaverJson);
        classifiedViolations = ViolationsSaverLoader.generateFromSaveString(pathToViolations, program.getSDG());
        callGraph = new JCallGraph();
        callGraph.generateCG(new File(pathToJar));
        BufferedReader br = new BufferedReader(new FileReader(pathToViolWrapper));
        StringBuilder completeString = new StringBuilder();
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (line.trim().startsWith("//")) {
                continue;
            }
            completeString.append(line + '\n');
        }
        violationsWrapper = ViolationsWrapper.generateFromSaveString(completeString.toString(), sdg, callGraph);
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

    public ViolationsWrapper generateNewViolWrapper() throws IOException {
        return new ViolationsWrapper(classifiedViolations, sdg, ana, callGraph);
    }

    private void addJsonStringToStringBuilder(StringBuilder stringBuilder, String key, String value) {
        stringBuilder.append("\"" + key + "\" : " + "\"" + value + "\"");
    }

    private void addKeyValueToJsonStringbuilder(StringBuilder stringBuilder, String key, String value) {
        stringBuilder.append("\"" + key + "\" : " + value);

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
        addKeyValueToJsonStringbuilder(created, "violation_wrapper", violationsWrapper.generateSaveString());
        created.append(",\n");

        created.append("}");
        return created.toString();
    }

    public static DisprovingProject generateFromSavestring(String s) {
        DisprovingProject disprovingProject = new DisprovingProject();
        return disprovingProject;
    }

}
