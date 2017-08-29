/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.IOException;
import java.util.Map;
import joanakeyrefactoring.AutomationHelper;
import joanakeyrefactoring.SummaryEdgeAndMethodToCorresData;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.javaforkeycreator.JavaForKeyCreator;
import joanakeyrefactoring.persistence.DisprovingProject;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public class JoanaKeyInterfacer {

    private ViolationsWrapper violationsWrapper;
    private JavaForKeyCreator javaForKeyCreator;
    private SummaryEdgeAndMethodToCorresData summaryEdgeToCorresData;
    private String pathToKey = "dependencies/Key/KeY.jar";
    private String pathToProofObs = "proofObs/proofs";

    public JoanaKeyInterfacer(
            DisprovingProject disprovingProject) throws IOException {
        this.violationsWrapper = disprovingProject.getViolationsWrapper();
        this.javaForKeyCreator
                = new JavaForKeyCreator(
                        disprovingProject.getPathToJava(),
                        disprovingProject.getCallGraph(),
                        disprovingProject.getSdg(),
                        disprovingProject.getStateSaver());
        Map<SDGEdge, StaticCGJavaMethod> summaryEdgesAndCorresJavaMethods = violationsWrapper.getSummaryEdgesAndCorresJavaMethods();
        summaryEdgeToCorresData = disprovingProject.getSummaryEdgeToCorresData();
    }

    public String getKeyContractFor(SDGNodeTuple formalTuple, StaticCGJavaMethod methodCorresToSE) {
        return summaryEdgeToCorresData.getContractFor(formalTuple);
    }

    public String getLoopInvariantFor(SDGEdge e, int index) {
        return summaryEdgeToCorresData.getLoopInvariantFor(e, index);
    }

    public void setLoopInvariantFor(SDGEdge e, int index, String val) {
        summaryEdgeToCorresData.setLoopInvariantFor(e, index, val);
    }

    public void resetLoopInvariant(SDGEdge currentSelectedEdge, int newValue) {
        summaryEdgeToCorresData.resetLoopInvariant(currentSelectedEdge, newValue);
    }

    public void openInKey(SDGEdge e,
            SDGNodeTuple tuple,
            StaticCGJavaMethod corresMethod) throws IOException {
        String pathToTestJava = javaForKeyCreator.
                generateJavaForFormalTupleCalledFromGui(
                        summaryEdgeToCorresData.getContractFor(tuple),
                        corresMethod,
                        summaryEdgeToCorresData.getEdgeToLoopInvariantTemplate().get(e),
                        summaryEdgeToCorresData.getEdgeToLoopInvariant().get(e),
                        summaryEdgeToCorresData.getMethodToMostGeneralContract()
                );
        AutomationHelper.openKeY(pathToKey, pathToProofObs);
    }

    public boolean tryDisproveEdge(
            SDGEdge e,
            SDGNodeTuple tuple,
            StaticCGJavaMethod corresMethod) throws IOException {
        String pathToTestJava = javaForKeyCreator.
                generateJavaForFormalTupleCalledFromGui(
                        summaryEdgeToCorresData.getContractFor(tuple),
                        corresMethod,
                        summaryEdgeToCorresData.getEdgeToLoopInvariantTemplate().get(e),
                        summaryEdgeToCorresData.getEdgeToLoopInvariant().get(e),
                        summaryEdgeToCorresData.getMethodToMostGeneralContract()
                );
        boolean worked = AutomationHelper.runKeY(pathToKey, pathToProofObs, "information flow");
        if (worked) {
            worked = AutomationHelper.runKeY(pathToKey, pathToProofObs, "functional");
        }
        if (worked) {
            violationsWrapper.removeEdge(e);
        }
        return worked;
    }

    public void markAsDisproved(SDGEdge currentSelectedEdge) {
        violationsWrapper.removeEdge(currentSelectedEdge);
    }

    public JavaForKeyCreator getJavaForKeyCreator() {
        return javaForKeyCreator;
    }

    public SummaryEdgeAndMethodToCorresData getSummaryEdgeToCorresData() {
        return summaryEdgeToCorresData;
    }
    
    

    
}
