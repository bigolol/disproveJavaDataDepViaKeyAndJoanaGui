/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring;

import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import java.util.ArrayList;
import java.util.Comparator;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public class SummaryEdgeComparator implements Comparator<SDGEdge> {

    private ViolationsWrapper violationsWrapper;

    public SummaryEdgeComparator(ViolationsWrapper violationsWrapper) {
        this.violationsWrapper = violationsWrapper;
    }

    @Override
    public int compare(SDGEdge e1, SDGEdge e2) {
        if (e1.equals(e2)) {
            return 0;
        }
        StaticCGJavaMethod e1Method = violationsWrapper.getMethodCorresToSummaryEdge(e1);
        StaticCGJavaMethod e2Method = violationsWrapper.getMethodCorresToSummaryEdge(e2);
        if (e1Method.callsFunction(e2Method) && !e2Method.callsFunction(e1Method)) {
            return -1;
        } else if (e2Method.callsFunction(e1Method) && !e1Method.callsFunction(e2Method)) {
            return 1;
        }
        return 0;
    }
}
