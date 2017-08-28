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
import java.util.List;
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
            ArrayList<String> loopInvList = new ArrayList<>();
            for (SDGNodeTuple t : allFormalPairs) {
                String contract = javaForKeyCreator.getMethodContractAndSetLoopInvariants(
                        t.getFirstNode(), t.getSecondNode(), edgesToMethods.get(e), loopInvList);
                formalTupleToContract.put(t, contract);
            }
            edgeToLoopInvariant.put(e, loopInvList);
        }
    }

    public String getContractFor(SDGNodeTuple t) {
        return formalTupleToContract.get(t);
    }
    
    public String getLoopInvariantFor(SDGEdge e, int index) {
        return edgeToLoopInvariant.get(e).get(index);
    }   
    
    public void setLoopInvariantFor(SDGEdge e, int index, String val) {
        edgeToLoopInvariant.get(e).set(index, val);
    }
    
}
