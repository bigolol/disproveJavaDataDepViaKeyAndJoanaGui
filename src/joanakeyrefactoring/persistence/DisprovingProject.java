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
    
    

}
