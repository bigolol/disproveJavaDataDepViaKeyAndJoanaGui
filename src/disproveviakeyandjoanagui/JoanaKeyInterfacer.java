/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import joanakeyrefactoring.StateSaver;
import joanakeyrefactoring.SummaryEdgeToCorresData;
import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.javaforkeycreator.JavaForKeyCreator;
import joanakeyrefactoring.javaforkeycreator.PointsToGenerator;
import joanakeyrefactoring.staticCG.JCallGraph;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public class JoanaKeyInterfacer {

    private ViolationsWrapper violationsWrapper;
    private JavaForKeyCreator javaForKeyCreator;
    private SummaryEdgeToCorresData summaryEdgeToCorresData;
    
    public JoanaKeyInterfacer(
            ViolationsWrapper violationsWrapper,
            String pathToJavaSource,
            JCallGraph callGraph,
            SDG sdg,
            StateSaver stateSaver) throws IOException {
        this.violationsWrapper = violationsWrapper;
        this.javaForKeyCreator = new JavaForKeyCreator(pathToJavaSource, callGraph, sdg, stateSaver);
        Map<SDGEdge, StaticCGJavaMethod> summaryEdgesAndCorresJavaMethods = violationsWrapper.getSummaryEdgesAndCorresJavaMethods();
        summaryEdgeToCorresData = new SummaryEdgeToCorresData(
                summaryEdgesAndCorresJavaMethods,
                sdg, 
                javaForKeyCreator);
    }

    public String getKeyContractFor(SDGNodeTuple formalTuple, StaticCGJavaMethod methodCorresToSE) {
        return summaryEdgeToCorresData.getEdgeFor(formalTuple);
    }
}
