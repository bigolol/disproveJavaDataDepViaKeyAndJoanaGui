package joanakeyrefactoring;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.util.intset.OrdinalSet;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;

import edu.kit.joana.wala.core.CGConsumer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import joanakeyrefactoring.persistence.PersistentCGNode;
import joanakeyrefactoring.persistence.PersistentLocalPointerKey;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * simple class to save intermediate results of SDG constructions (i.e.
 * points-to and call graph) for later use
 */
public class StateSaver implements CGConsumer {

    private CallGraph callGraph;
    private PointerAnalysis<? extends InstanceKey> pointerAnalyis;
    private List<PersistentLocalPointerKey> persistentLocalPointerKeys = new ArrayList<>();
    private List<PersistentCGNode> persistentCGNodes = new ArrayList<>();
    private Map<Integer, PersistentCGNode> cgNodeIdToPersistentCGNodes = new HashMap<>();
    private Map<CGNode, PersistentCGNode> cgNodesToPersistentCGNodes = new HashMap<>();
    private Map<PersistentLocalPointerKey, List<PersistentLocalPointerKey>> disjunctPointsToSets
            = new HashMap<>();

    @Override
    public void consume(CallGraph callGraph, PointerAnalysis<? extends InstanceKey> pointerAnalyis) {
        this.callGraph = callGraph;
        this.pointerAnalyis = pointerAnalyis;
    }

    public static StateSaver generateFromJson(JSONObject jsonObj) throws IOException {
        StateSaver stateSaver = new StateSaver();

        JSONArray cgNodeArr = jsonObj.getJSONArray("cgNodes");

        for (int i = 0; i < cgNodeArr.length(); ++i) {
            JSONObject currentCGNodeSaveObj = cgNodeArr.getJSONObject(i);
            PersistentCGNode currentPersistentCGNode = PersistentCGNode.generateFromJsonObj(currentCGNodeSaveObj);
            stateSaver.persistentCGNodes.add(currentPersistentCGNode);
            stateSaver.cgNodeIdToPersistentCGNodes.put(currentPersistentCGNode.getCgNodeId(), currentPersistentCGNode);
        }

        JSONArray pointerKeyArr = jsonObj.getJSONArray("localPointerKeys");
        for (int i = 0; i < pointerKeyArr.length(); ++i) {
            JSONObject currentPointerKeyJsonObj = pointerKeyArr.getJSONObject(i);
            PersistentLocalPointerKey currentLocalPointerKey = PersistentLocalPointerKey.generateFromJsonObj(currentPointerKeyJsonObj, stateSaver.persistentCGNodes);
            stateSaver.persistentLocalPointerKeys.add(currentLocalPointerKey);
        }

        JSONArray disjunctPointstoArr = jsonObj.getJSONArray("disjunctPointsTo");
        for (int i = 0; i < disjunctPointstoArr.length(); ++i) {
            JSONObject currentDisjObj = disjunctPointstoArr.getJSONObject(i);
            int key = currentDisjObj.getInt("key");
            JSONArray values = currentDisjObj.getJSONArray("values");
            List<PersistentLocalPointerKey> disPointers = new ArrayList<>();
            for (int j = 0; j < values.length(); ++j) {
                int currDisIndex = values.getInt(j);
                disPointers.add(stateSaver.persistentLocalPointerKeys.get(currDisIndex));
            }
            PersistentLocalPointerKey keyPointerKey = stateSaver.persistentLocalPointerKeys.get(key);
            stateSaver.disjunctPointsToSets.put(keyPointerKey, disPointers);
        }

        return stateSaver;
    }

    public String getSaveString() {
        StringBuilder created = new StringBuilder();
        created.append("\"cgNodes\" : [").append('\n');
        for (PersistentCGNode persistentCGNode : persistentCGNodes) {
            created.append("{");
            created.append(persistentCGNode.generateSaveString());
            created.append("},\n");
        }
        if (created.lastIndexOf("[") != created.length() - 1) {
            created.replace(created.length() - 2, created.length(), "");
        }
        created.append("],\n");

        created.append("\"localPointerKeys\" : [");
        for (PersistentLocalPointerKey persistentLocalPointerKey : persistentLocalPointerKeys) {
            created.append("{");
            created.append(persistentLocalPointerKey.generateSaveString()).append('\n');
            created.append("},\n");
        }
        if (created.lastIndexOf("[") != created.length() - 1) {
            created.replace(created.length() - 2, created.length(), "");
        }
        created.append("],\n");

        created.append("\"disjunctPointsTo\" : [");

        disjunctPointsToSets.forEach((k, l) -> {
            created.append("{");
            created.append("\"key\" : " + k.getId()).append(", \"values\" : [ ");
            l.forEach((t) -> {
                created.append(t.getId()).append(", ");
            });
            if (created.lastIndexOf("[") != created.length() - 1) {
                created.replace(created.length() - 2, created.length(), "");
            }
            created.append("]\n");
            created.append("},\n");
        });
        if (created.lastIndexOf("[") != created.length() - 1) {
            created.replace(created.length() - 2, created.length(), "");
        }
        created.append("]\n");

        return created.toString();
    }

    public boolean pointsToSetsAreDisjunct(PersistentLocalPointerKey n1, PersistentLocalPointerKey n2) {
        return true;
    }

    public List<PersistentLocalPointerKey> getDisjunctLPKs(PersistentLocalPointerKey persistentLocalPointerKey) {
        return disjunctPointsToSets.get(persistentLocalPointerKey);
    }

    public void generatePersistenseStructures(SDG sdg) {

        ArrayList<LocalPointerKey> localPointerKeys = new ArrayList<>();

        int id = 0;

        for (PointerKey pk : pointerAnalyis.getPointerKeys()) {
            if (pk instanceof LocalPointerKey) {
                LocalPointerKey localPointerKey = (LocalPointerKey) pk;

                if (!localPointerKey.isParameter()) {
                    continue;
                }

                CGNode corresCgNode = localPointerKey.getNode();
                localPointerKeys.add(localPointerKey);

                if (!cgNodesToPersistentCGNodes.containsKey(corresCgNode)) {
                    PersistentCGNode persistentCGNode = new PersistentCGNode(id);
                    cgNodesToPersistentCGNodes.put(corresCgNode, persistentCGNode);
                    persistentCGNodes.add(persistentCGNode);
                    id++;
                }
            }
        }

        Set<SDGEdge> edgeSet = sdg.edgeSet();
        for (SDGEdge e : edgeSet) {
            if (e.getKind() == SDGEdge.Kind.SUMMARY) {
                SDGNode actualInNode = e.getSource();
                SDGNode actualOutNode = e.getTarget();
                Collection<SDGNodeTuple> formalNodePairs = sdg.getAllFormalPairs(actualInNode, actualOutNode);
                for (SDGNodeTuple formalNodeTuple : formalNodePairs) {
                    SDGNode formalInNode = formalNodeTuple.getFirstNode();
                    SDGNode methodNode = sdg.getEntry(formalInNode);
                    int cgNodeId = sdg.getCGNodeId(methodNode);
                    CGNode cgNode = callGraph.getNode(cgNodeId);
                    PersistentCGNode corresPersCGNode = cgNodesToPersistentCGNodes.get(cgNode);
                    if (corresPersCGNode != null) {
                        corresPersCGNode.setCgNodeId(cgNodeId);
                    }
                }
            }
        }

        generateIRsAndPersistentLocalPointerKeys(localPointerKeys);
        calculateDisjunctPointsToKeys(localPointerKeys);
    }

    private void generateIRsAndPersistentLocalPointerKeys(ArrayList<LocalPointerKey> localPointerKeys) {
        for (int i = 0; i < localPointerKeys.size(); ++i) {
            LocalPointerKey currentLocalPointerKey = localPointerKeys.get(i);
            CGNode cgNode = currentLocalPointerKey.getNode();
            PersistentCGNode persistentCGNode = cgNodesToPersistentCGNodes.get(cgNode);
            persistentCGNode.createPersistentIR(cgNode, localPointerKeys);

            Integer currentCGNodeId = persistentCGNode.getCgNodeId();
            cgNodeIdToPersistentCGNodes.put(currentCGNodeId, persistentCGNode);

            PersistentLocalPointerKey persistentLocalPointerKey = new PersistentLocalPointerKey(currentLocalPointerKey, persistentCGNode, i);
            persistentLocalPointerKeys.add(persistentLocalPointerKey);
        }
    }

    private void calculateDisjunctPointsToKeys(ArrayList<LocalPointerKey> localPointerKeys) {
        for (int i = 0; i < localPointerKeys.size(); ++i) {
            OrdinalSet<? extends InstanceKey> currentPointsToset = pointerAnalyis.getPointsToSet(localPointerKeys.get(i));
            for (int j = 0; j < localPointerKeys.size(); ++j) {
                if (i == j) {
                    continue;
                } else if (!localPointerKeys.get(i).getNode().equals(localPointerKeys.get(j).getNode())) {
                    continue;
                }
                OrdinalSet<? extends InstanceKey> otherPointsToSet = pointerAnalyis.getPointsToSet(localPointerKeys.get(j));
                if (disjunct(currentPointsToset, otherPointsToSet)) {
                    if (disjunctPointsToSets.containsKey(persistentLocalPointerKeys.get(i))) {
                        disjunctPointsToSets.get(persistentLocalPointerKeys.get(i)).add(persistentLocalPointerKeys.get(j));
                    } else {
                        List<PersistentLocalPointerKey> list = new ArrayList<>();
                        list.add(persistentLocalPointerKeys.get(j));
                        disjunctPointsToSets.put(persistentLocalPointerKeys.get(i), list);
                    }
                }
            }
        }
    }

    private static boolean disjunct(OrdinalSet<?> s1, OrdinalSet<?> s2) {
        for (Object e1 : s1) {
            for (Object e2 : s2) {
                if (e1.equals(e2)) {
                    return false;
                }
            }
        }
        return true;
    }

    public PersistentCGNode getNode(int nodeID) {
        return cgNodeIdToPersistentCGNodes.get(nodeID);
    }

    public List<PersistentLocalPointerKey> getPersistentLocalPointerKeys(PersistentCGNode cGNode) {
        List<PersistentLocalPointerKey> created = new ArrayList<>();
        for (PersistentLocalPointerKey persistentLocalPointerKey : persistentLocalPointerKeys) {
            if (persistentLocalPointerKey.getNode().equals(cGNode)) {
                created.add(persistentLocalPointerKey);
            }
        }
        return created;
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
        final StateSaver other = (StateSaver) obj;

        if (persistentCGNodes.size() != other.persistentCGNodes.size()) {
            return false;
        }

        for (int i = 0; i < persistentCGNodes.size(); ++i) {
            if (!persistentCGNodes.get(i).equals(other.persistentCGNodes.get(i))) {
                return false;
            }
        }

        for (Integer k : cgNodeIdToPersistentCGNodes.keySet()) {
            if (!cgNodeIdToPersistentCGNodes.get(k).equals(other.cgNodeIdToPersistentCGNodes.get(k))) {
                return false;
            }
        }

        if (persistentLocalPointerKeys.size() != other.persistentLocalPointerKeys.size()) {
            return false;
        }

        for (int i = 0; i < persistentLocalPointerKeys.size(); ++i) {
            if (!persistentLocalPointerKeys.get(i).equals(other.persistentLocalPointerKeys.get(i))) {
                return false;
            }
        }

        for (PersistentLocalPointerKey k : disjunctPointsToSets.keySet()) {
            if (!disjunctPointsToSets.get(k).equals(other.disjunctPointsToSets.get(k))) {
                return false;
            }
        }

        return true;
    }

}
