/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.chopper.Chopper;
import edu.kit.joana.ifc.sdg.graph.chopper.RepsRosayChopper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author holgerklein
 */
public class ViolationChop {

    public static final String SOURCE = "src";
    public static final String SINK = "sink";
    private static final String NODES_IN_CHOP = "nodes_in_chop";

    private SDGNode violationSource;
    private SDGNode violationSink;
    private Collection<SDGNode> nodesInChop;
    private SDG inducedSubgraph;
    private Chopper chopper;
    private List<SDGEdge> summaryEdges = new ArrayList<>();

    public ViolationChop(SDGNode violationSource, SDGNode violationSink, SDG sdg) {
        this.violationSource = violationSource;
        this.violationSink = violationSink;
        findSummaryEdges(sdg);
    }

    private ViolationChop() {
    }

    public String generateSaveString() {
        StringBuilder created = new StringBuilder();
        created.append("{").append(System.lineSeparator());
        int lengthOfLineSep = System.lineSeparator().length();

        created.append("\"" + SOURCE + "\" : ")
            .append(violationSource.getId()).append(",").append(System.lineSeparator());
        created.append("\"" + SINK + "\" : ")
            .append(violationSink.getId()).append(",").append(System.lineSeparator());

        created.append("\"" + NODES_IN_CHOP + "\" : [").append(System.lineSeparator());
        for (SDGNode n : nodesInChop) {
            created.append(n.getId()).append(",").append(System.lineSeparator());
        }
        if (created.lastIndexOf("[") != created.length() - 1) {
            created.delete(created.length() - lengthOfLineSep - 1, created.length());
        }
        created.append("]").append(System.lineSeparator());
        created.append("}");
        return created.toString();
    }

    public static ViolationChop generateFromJsonObj(JSONObject jSONObject, SDG sdg) {

        int srcId = jSONObject.getInt(SOURCE);
        int sinkId = jSONObject.getInt(SINK);
        ViolationChop created = new ViolationChop();
        created.violationSource = sdg.getNode(srcId);
        created.violationSink = sdg.getNode(sinkId);
        created.nodesInChop = new HashSet<>();
        JSONArray nodesInChopArr = jSONObject.getJSONArray(NODES_IN_CHOP);
        for (int i = 0; i < nodesInChopArr.length(); ++i) {
            created.nodesInChop.add(sdg.getNode(nodesInChopArr.getInt(i)));
        }
        created.inducedSubgraph = sdg.subgraph(created.nodesInChop);
        created.inducedSubgraph.edgeSet().forEach((e) -> {
            if (isSummaryEdge(e)) {
                created.summaryEdges.add(e);
            }
        });
        return created;
    }

    public SDGNode getViolationSource() {
        return violationSource;
    }

    public SDGNode getViolationSink() {
        return violationSink;
    }

    public boolean isEmpty() {
        return nodesInChop.isEmpty();
    }

    public Collection<SDGEdge> getSummaryEdges() {
        return summaryEdges;
    }

    public void findSummaryEdges(SDG sdg) {
        this.chopper = new RepsRosayChopper(sdg);
        nodesInChop = chopper.chop(violationSource, violationSink);
        if (nodesInChop.isEmpty()) {
            return;
        }
        inducedSubgraph = sdg.subgraph(nodesInChop);

        inducedSubgraph.edgeSet().forEach((e) -> {
            if (isSummaryEdge(e)) {
                summaryEdges.add(e);
            }
        });
    }

    private static boolean isSummaryEdge(SDGEdge currentEdge) {
        return currentEdge.getKind() == SDGEdge.Kind.SUMMARY;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ViolationChop other = (ViolationChop) obj;
        if (!Objects.equals(this.violationSource, other.violationSource)) {
            return false;
        }
        if (!Objects.equals(this.violationSink, other.violationSink)) {
            return false;
        }
        for(int i = 0; i < summaryEdges.size(); ++i) {
            if(!summaryEdges.get(i).equals(other.summaryEdges.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    
}
