/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import joanakeyrefactoring.javaforkeycreator.JavaForKeyCreator;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public class SummaryEdgeToCorresData {

    private HashMap<SDGNodeTuple, String> formalTupleToContract = new HashMap<>();
    private HashMap<SDGEdge, ArrayList<String>> edgeToLoopInvariant = new HashMap<>();

    public SummaryEdgeToCorresData(
            Map<SDGEdge, StaticCGJavaMethod> edgesToMethods,
            SDG sdg,
            JavaForKeyCreator javaForKeyCreator) throws IOException {
        for (SDGEdge e : edgesToMethods.keySet()) {
            Collection<SDGNodeTuple> allFormalPairs = sdg.getAllFormalPairs(e.getSource(), e.getTarget());
            for (SDGNodeTuple t : allFormalPairs) {
                String contract = javaForKeyCreator.getMethodContractFor(t.getFirstNode(), t.getSecondNode(), edgesToMethods.get(e));
                formalTupleToContract.put(t, contract);
            }
        }
    }

    public String getEdgeFor(SDGNodeTuple t) {
        return formalTupleToContract.get(t);
    }
}
