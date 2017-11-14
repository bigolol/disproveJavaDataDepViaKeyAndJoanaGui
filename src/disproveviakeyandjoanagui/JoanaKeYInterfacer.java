/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.IOException;
import joanakeyrefactoring.AutomationHelper;
import joanakeyrefactoring.SummaryEdgeAndMethodToCorresData;
import joanakeyrefactoring.ViolationsDisproverSemantic;
import joanakeyrefactoring.ViolationsDisproverSemantic.PO_TYPE;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.javaforkeycreator.JavaForKeYCreator;
import joanakeyrefactoring.persistence.DisprovingProject;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public class JoanaKeYInterfacer {

    private ViolationsWrapper violationsWrapper;
    private JavaForKeYCreator javaForKeyCreator;
    private SummaryEdgeAndMethodToCorresData summaryEdgeToCorresData;
    public final static String PATH_TO_KeY =
            AutomationHelper.DEPENDENCIES_FOLDER + "Key/KeY.jar";

    public JoanaKeYInterfacer(
            DisprovingProject disprovingProject) throws IOException {
        this.violationsWrapper = disprovingProject.getViolationsWrapper();
        this.javaForKeyCreator
                = new JavaForKeYCreator(
                        disprovingProject.getPathToJava(),
                        disprovingProject.getCallGraph(),
                        disprovingProject.getSdg(),
                        disprovingProject.getStateSaver());
        //Map<SDGEdge, StaticCGJavaMethod> summaryEdgesAndCorresJavaMethods =
        violationsWrapper.getSummaryEdgesAndCorresJavaMethods();
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
        //String pathToTestJava =
                javaForKeyCreator.
                generateJavaForFormalTupleCalledFromGui(
                        summaryEdgeToCorresData.getContractFor(tuple),
                        corresMethod,
                        summaryEdgeToCorresData.getEdgeToLoopInvariantTemplate().get(e),
                        summaryEdgeToCorresData.getEdgeToLoopInvariant().get(e),
                        summaryEdgeToCorresData.getMethodToMostGeneralContract()
                );
        AutomationHelper.openKeY(PATH_TO_KeY, ViolationsDisproverSemantic.PO_PATH);
    }

    public boolean tryDisproveEdge(
            SDGEdge e,
            SDGNodeTuple tuple,
            StaticCGJavaMethod corresMethod) throws IOException {
        if (corresMethod == null) {
            throw new IOException("No method selected!");
        }
        //String pathToTestJava =
        javaForKeyCreator.generateJavaForFormalTupleCalledFromGui(
                summaryEdgeToCorresData.getContractFor(tuple),
                corresMethod,
                summaryEdgeToCorresData.getEdgeToLoopInvariantTemplate().get(e),
                summaryEdgeToCorresData.getEdgeToLoopInvariant().get(e),
                summaryEdgeToCorresData.getMethodToMostGeneralContract()
                );
        boolean worked = AutomationHelper.runKeY(PATH_TO_KeY,
                                                 ViolationsDisproverSemantic.PO_PATH,
                                                 PO_TYPE.INFORMATION_FLOW);
        if (worked) {
            worked = AutomationHelper.runKeY(PATH_TO_KeY,
                                             ViolationsDisproverSemantic.PO_PATH,
                                             PO_TYPE.FUNCTIONAL);
        }
        if (worked) {
            violationsWrapper.removeEdge(e);
        }
        return worked;
    }

    public void markAsDisproved(SDGEdge currentSelectedEdge) {
        violationsWrapper.removeEdge(currentSelectedEdge);
    }

    public JavaForKeYCreator getJavaForKeyCreator() {
        return javaForKeyCreator;
    }

    public SummaryEdgeAndMethodToCorresData getSummaryEdgeToCorresData() {
        return summaryEdgeToCorresData;
    }
    
    

    
}
