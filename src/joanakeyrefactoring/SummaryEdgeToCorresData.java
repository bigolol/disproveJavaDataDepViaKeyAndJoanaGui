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
    private HashMap<SDGEdge, String> edgeToLoopInvariantTemplate = new HashMap<>();

    public SummaryEdgeToCorresData(
            Map<SDGEdge, StaticCGJavaMethod> edgesToMethods,
            SDG sdg,
            JavaForKeyCreator javaForKeyCreator) throws IOException {
        for (SDGEdge e : edgesToMethods.keySet()) {            
            Collection<SDGNodeTuple> allFormalPairs = sdg.getAllFormalPairs(e.getSource(), e.getTarget());
            for (SDGNodeTuple t : allFormalPairs) {
                String contract = javaForKeyCreator.getMethodContractAndSetLoopInvariant(
                        t.getFirstNode(), t.getSecondNode(), edgesToMethods.get(e),
                        edgeToLoopInvariantTemplate, e);
                formalTupleToContract.put(t, contract);
            }
            ArrayList<String> loopInvariants = new ArrayList<>();
            for(int i = 0; i < edgesToMethods.get(e).getRelPosOfLoops().size(); ++i) {
                loopInvariants.add(null);
            }
            edgeToLoopInvariant.put(e, loopInvariants);
        }
    }

    public String getContractFor(SDGNodeTuple t) {
        return formalTupleToContract.get(t);
    }
    
    public String getLoopInvariantFor(SDGEdge e, int index) {
        String get = edgeToLoopInvariant.get(e).get(index);
        if(get == null) {
            return edgeToLoopInvariantTemplate.get(e);
        }
        return get;
    }   
    
    public void setLoopInvariantFor(SDGEdge e, int index, String val) {
        edgeToLoopInvariant.get(e).set(index, val);
    }

    public void resetLoopInvariant(SDGEdge currentSelectedEdge, int newValue) {
        edgeToLoopInvariant.get(currentSelectedEdge).set(newValue, null);
    }
    
}
