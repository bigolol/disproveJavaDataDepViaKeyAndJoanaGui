/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import joanakeyrefactoring.StateSaver;
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

    public JoanaKeyInterfacer(
            ViolationsWrapper violationsWrapper,
            String pathToJavaSource,
            JCallGraph callGraph,
            SDG sdg,
            StateSaver stateSaver) {
        this.violationsWrapper = violationsWrapper;
        this.javaForKeyCreator = new JavaForKeyCreator(pathToJavaSource, callGraph, sdg, stateSaver);
    }

    public String getKeyContractFor(SDGNodeTuple formalTuple, StaticCGJavaMethod methodCorresToSE) {
        return javaForKeyCreator.getMethodContractFor(
                formalTuple.getFirstNode(), formalTuple.getSecondNode(), methodCorresToSE);
    }
}
