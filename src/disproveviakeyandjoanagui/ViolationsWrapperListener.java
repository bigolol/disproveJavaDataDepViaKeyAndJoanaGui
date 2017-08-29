/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import java.util.List;
import java.util.Map;
import joanakeyrefactoring.ViolationChop;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public interface ViolationsWrapperListener {
    public void parsedChop(ViolationChop chop);
    public void disprovedEdge(SDGEdge e);
    public void disprovedChop(ViolationChop chop);
    public void disprovedAll();
    public void addedNewEdges(Map<SDGEdge, StaticCGJavaMethod> edgesToMethods, List<SDGEdge> edgesSorted, SDG sdg);
}
