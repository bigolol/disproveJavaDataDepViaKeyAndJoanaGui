/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.util.intset.OrdinalSet;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import joanakeyrefactoring.StateSaver;
import joanakeyrefactoring.persistence.PersistentCGNode;
import joanakeyrefactoring.persistence.PersistentIR;
import joanakeyrefactoring.persistence.PersistentLocalPointerKey;

/**
 *
 * @author holger
 */
public class PointsToGenerator {

    /**
     * Calculates non-aliasing information for parameters of a method node using
     * JOANA's points-to information. From the paper: Definition 9 (Generation
     * of preconditions). Let o be a reference and P_o its points-to set. We
     * generate the following precondition: for all o' in P_o: o = o'
     *
     * @param sdg our SDG
     * @param methodNode method node to check
     * @return non-aliasing information as a string that can be used as
     * precondition
     */
    public static String generatePreconditionFromPointsToSet(SDG sdg, SDGNode methodNode, StateSaver stateSaver) {
        //get the call graph node corresponding to the SDG method node
        PersistentCGNode persistentCGNode = stateSaver.getCGNodeForFormalIn(methodNode);
        // get IR for parameter names
        PersistentIR persistentIR = persistentCGNode.getIR();
        List<PersistentLocalPointerKey> persistentLocalPointerKeys = stateSaver.getPersistentLocalPointerKeys(persistentCGNode);

        // calculate individual non-alias clauses
        Set<String> pointsToResult = calculateNonAliases(persistentLocalPointerKeys, stateSaver, persistentIR);
        StringBuilder stringBuilder = new StringBuilder();
        String delim = "";
        //chain clauses together by conjunction
        for (String nonAliased : pointsToResult) {
            stringBuilder.append(delim).append(nonAliased);
            delim = " && ";
        }
        /**
         * for simpler code, if we don't have any clauses, we return "true" here
         * instead of writing code that does not emit a aliasing precondition.
         * do it the proper way
         */
        if (stringBuilder.toString().equals("")) {
            return "true";
        }
        return stringBuilder.toString();
    }
    
    private static Set<String> calculateNonAliases(
            List<PersistentLocalPointerKey> localPointerKeys,
            StateSaver stateSaver, PersistentIR ir) {
        Set<String> result = new HashSet<String>();
        // enumerate all two element subsets of pointer keys and check if those two have disjunct points-to sets
        for (PersistentLocalPointerKey persistentLocalPointerKey : localPointerKeys) {
            String o1 = ir.getLocalName(persistentLocalPointerKey.getValueNumber());
            List<PersistentLocalPointerKey> disjunctLPKs = stateSaver.getDisjunctLPKs(persistentLocalPointerKey);
            if (disjunctLPKs == null) {
                continue;
            }
            for (PersistentLocalPointerKey currentDisjunctOtherPk : disjunctLPKs) {
                String o2 = ir.getLocalName(currentDisjunctOtherPk.getValueNumber());
                // if points-to sets are disjunct, o1 and o2 cannot alias
                if (o1 != null && o2 != null) {
                    if (!result.contains(o1 + " != " + o2)) {
                        result.add(o1 + " != " + o2);
                    }
                }
            }
        }
        return result;
    }
    
}
