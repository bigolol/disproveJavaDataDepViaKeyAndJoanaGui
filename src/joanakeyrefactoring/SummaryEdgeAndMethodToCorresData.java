/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring;

import disproveviakeyandjoanagui.ErrorLogger;
import disproveviakeyandjoanagui.ViolationsWrapperListener;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import joanakeyrefactoring.javaforkeycreator.JavaForKeYCreator;
import joanakeyrefactoring.staticCG.JCallGraph;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author holger
 */
public class SummaryEdgeAndMethodToCorresData implements ViolationsWrapperListener {

    private final static String FORMAL_NODE_TUPLE_TO_CONTRACT = "formal_node_tuple_to_contract";
    private final static String EDGE_TO_LOOP_INVARIANT_TEMPLATE = "edge_to_loop_invariant_template";
    private final static String EDGE_TO_LOOP_INVARIANT = "edge_to_loop_invariant";
    private final static String METHOD_TO_MOST_GENERAL_CONTRACT = "method_to_most_general_contract";
    private final static String MOST_GENERAL_CONTRACT = "most_general_contract";
    private final static String LOOP_INVARIANTS = "loop_invariants";
    private final static String NULL = "null";
    private final static String IN = "in";
    private final static String OUT = "out";
    private final static String CONTRACT = "contract";
    private final static String INVARIANT = "invariant";
    private final static String METHOD = "method";

    private HashMap<SDGNodeTuple, String> formalTupleToContract = new HashMap<>();
    private HashMap<SDGEdge, ArrayList<String>> edgeToLoopInvariant = new HashMap<>();
    private HashMap<SDGEdge, String> edgeToLoopInvariantTemplate = new HashMap<>();
    private HashMap<StaticCGJavaMethod, String> methodToMostGeneralContract = new HashMap<>();
    private JavaForKeYCreator javaForKeyCreator;

    public SummaryEdgeAndMethodToCorresData(
            Map<SDGEdge, StaticCGJavaMethod> edgesToMethods,
            SDG sdg,
            JavaForKeYCreator javaForKeyCreator) throws IOException {
        this.javaForKeyCreator = javaForKeyCreator;
        for (SDGEdge e : edgesToMethods.keySet()) {
            Collection<SDGNodeTuple> allFormalPairs = sdg.getAllFormalPairs(e.getSource(), e.getTarget());
            for (SDGNodeTuple t : allFormalPairs) {
                String contract = javaForKeyCreator
                        .getMethodContractAndSetLoopInvariantAndSetMostGeneralContract(
                                t.getFirstNode(), t.getSecondNode(), edgesToMethods.get(e),
                                edgeToLoopInvariantTemplate, e, methodToMostGeneralContract);
                formalTupleToContract.put(t, contract);
            }
            ArrayList<String> loopInvariants = new ArrayList<>();
            for (int i = 0; i < edgesToMethods.get(e).getRelPosOfLoops().size(); ++i) {
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
        if (get == null) {
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

    public String getMostGeneralContractFor(StaticCGJavaMethod method) {
        return methodToMostGeneralContract.get(method);
    }

    private String escapeContract(String contract) {
        for (int i = 0; i < contract.length(); ++i) {
            if (contract.charAt(i) == '\\') {
                contract = contract.substring(0, i) + '\\' + '\\' + contract.substring(i + 1);
                i += 2;
            } else if (contract.charAt(i) == '\n') {
                contract = contract.substring(0, i) + contract.substring(i + 1);
            }
        }
        return contract;
    }

    public HashMap<SDGNodeTuple, String> getFormalTupleToContract() {
        return formalTupleToContract;
    }

    public HashMap<SDGEdge, ArrayList<String>> getEdgeToLoopInvariant() {
        return edgeToLoopInvariant;
    }

    public HashMap<SDGEdge, String> getEdgeToLoopInvariantTemplate() {
        return edgeToLoopInvariantTemplate;
    }

    public HashMap<StaticCGJavaMethod, String> getMethodToMostGeneralContract() {
        return methodToMostGeneralContract;
    }

    public String generateSaveString(ViolationsWrapper violationsWrapper) {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"" + FORMAL_NODE_TUPLE_TO_CONTRACT + "\" : [");
        formalTupleToContract.forEach((t, c) -> {
            builder.append("{");
            builder.append("\"" + IN + "\" : " + t.getFirstNode().getId() + ", ");
            builder.append("\"" + OUT + "\" : " + t.getSecondNode().getId() + ", ");
            builder.append("\"" + CONTRACT + "\" : \"" + escapeContract(c) + "\"");
            builder.append("},\n");
        });
        if (builder.lastIndexOf(",") == builder.length() - 2) {
            builder.replace(builder.length() - 2, builder.length(), "");
        }
        builder.append("],");
        builder.append("\"" + EDGE_TO_LOOP_INVARIANT_TEMPLATE + "\" : [");
        edgeToLoopInvariantTemplate.forEach((e, s) -> {
            builder.append("{");
            builder.append("\"" + IN + "\" : " + e.getSource().getId() + ", ");
            builder.append("\"" + OUT + "\" : " + e.getTarget().getId() + ", ");
            builder.append("\"" + INVARIANT + "\" : \"" + escapeContract(s) + "\"");
            builder.append("},\n");
        });
        if (builder.lastIndexOf(",") == builder.length() - 2) {
            builder.replace(builder.length() - 2, builder.length(), "");
        }
        builder.append("],");
        builder.append("\"" + METHOD_TO_MOST_GENERAL_CONTRACT + "\" : [");
        methodToMostGeneralContract.forEach((m, c) -> {
            builder.append("{");
            builder.append("\"" + METHOD + "\" : \""
                    + m.getContainingClass().getId()
                    + "/" + m.getId()
                    + "/" + m.getParameterWithoutPackage() + "\", ");
            builder.append("\"" + MOST_GENERAL_CONTRACT + "\" : \"" + escapeContract(c) + "\", ");
            builder.append("},\n");
        });
        if (builder.lastIndexOf(",") == builder.length() - 2) {
            builder.replace(builder.length() - 2, builder.length(), "");
        }
        builder.append("],");
        builder.append("\"" + EDGE_TO_LOOP_INVARIANT + "\" : [");
        edgeToLoopInvariant.forEach((e, i) -> {
            builder.append("{");
            builder.append("\"" + IN + "\" : " + e.getSource().getId() + ", ");
            builder.append("\"" + OUT + "\" : " + e.getTarget().getId() + ", ");
            builder.append("\"" + LOOP_INVARIANTS + "\" : [");
            i.forEach((s) -> {
                if (s == null) {
                    builder.append("\""+ NULL + "\"");
                } else {
                    builder.append("\"" + escapeContract(s) + "\",");
                }
            });
            if (builder.lastIndexOf(",") == builder.length() - 1) {
                builder.replace(builder.length() - 1, builder.length(), "");
            }
            builder.append("]");
            builder.append("},\n");
        });
        if (builder.lastIndexOf(",") == builder.length() - 2) {
            builder.replace(builder.length() - 2, builder.length(), "");
        }
        builder.append("]");
        builder.append("}");
        return builder.toString();
    }

    private SummaryEdgeAndMethodToCorresData() {
    }

    private static String addLinebreaksBackIntoContract(String c) {
        for (int i = 0; i < c.length(); ++i) {
            if (c.charAt(i) == '\t') {
                if (i == 0) {
                    //do nothing
                } else {
                    c = c.substring(0, i) + '\n' + c.substring(i);
                }
                i++;
                while (c.charAt(i) == '\t') {
                    ++i;
                }
            }
        }
        return c;
    }

    public static SummaryEdgeAndMethodToCorresData
            generateFromjson(JSONObject jsonObject, JCallGraph callGraph, SDG sdg) {
        SummaryEdgeAndMethodToCorresData created = new SummaryEdgeAndMethodToCorresData();
        JSONArray currentJsonArray = jsonObject.getJSONArray(FORMAL_NODE_TUPLE_TO_CONTRACT);
        for (int i = 0; i < currentJsonArray.length(); ++i) {
            JSONObject currentJsonObj = currentJsonArray.getJSONObject(i);
            int idin = currentJsonObj.getInt(IN);
            int idout = currentJsonObj.getInt(OUT);
            String contract
                    = addLinebreaksBackIntoContract(currentJsonObj.getString(CONTRACT));
            SDGNode in = sdg.getNode(idin);
            SDGNode out = sdg.getNode(idout);
            SDGNodeTuple sdgNodeTuple = new SDGNodeTuple(in, out);
            created.formalTupleToContract.put(sdgNodeTuple, contract);
        }
        currentJsonArray = jsonObject.getJSONArray(EDGE_TO_LOOP_INVARIANT_TEMPLATE);
        for (int i = 0; i < currentJsonArray.length(); ++i) {
            JSONObject currentJsonObj = currentJsonArray.getJSONObject(i);
            int idin = currentJsonObj.getInt(IN);
            int idout = currentJsonObj.getInt(OUT);
            String invariantTemplate = addLinebreaksBackIntoContract(
                    currentJsonObj.getString(INVARIANT));
            SDGNode in = sdg.getNode(idin);
            SDGNode out = sdg.getNode(idout);
            Set<SDGEdge> allEdges = sdg.getAllEdges(in, out);
            //SDGEdge found = null;
            for (SDGEdge e : allEdges) {
                if (e.getKind() == SDGEdge.Kind.SUMMARY) {
                    //found = e;
                    created.edgeToLoopInvariantTemplate.put(e, invariantTemplate);
                    break;
                }
            }
        }

        currentJsonArray = jsonObject.getJSONArray(METHOD_TO_MOST_GENERAL_CONTRACT);
        for (int i = 0; i < currentJsonArray.length(); ++i) {
            JSONObject currentJsonObj = currentJsonArray.getJSONObject(i);
            String methodDescr = currentJsonObj.getString(METHOD);
            String[] split = methodDescr.split("/");
            String classId = split[0];
            String methodid = split[1];
            String argtypes;
            if (split.length == 3) {
                argtypes = split[2];
            } else {
                argtypes = "";
            }
            String mostGeneralContract = currentJsonObj.getString(MOST_GENERAL_CONTRACT);
            StaticCGJavaMethod method = callGraph.getMethodFor(classId, methodid, argtypes);
            created.methodToMostGeneralContract.put(method, mostGeneralContract);
        }

        currentJsonArray = jsonObject.getJSONArray(EDGE_TO_LOOP_INVARIANT);
        for (int i = 0; i < currentJsonArray.length(); ++i) {
            JSONObject currentJsonObj = currentJsonArray.getJSONObject(i);
            int inid = currentJsonObj.getInt(IN);
            int outid = currentJsonObj.getInt(OUT);
            JSONArray loopInvArrs = currentJsonObj.getJSONArray(LOOP_INVARIANTS);
            ArrayList<String> loopInvariants = new ArrayList<>();
            for (int j = 0; j < loopInvArrs.length(); ++j) {
                String string = loopInvArrs.getString(j);
                if (string.equals(NULL)) {
                    loopInvariants.add(null);
                } else {
                    loopInvariants.add(string);
                }
            }
            SDGEdge e = sdg.getEdge(sdg.getNode(inid), sdg.getNode(outid));
            created.edgeToLoopInvariant.put(e, loopInvariants);
        }
        return created;
    }

    @Override
    public void parsedChop(ViolationChop chop) {
    }

    @Override
    public void disprovedEdge(SDGEdge e) {
    }

    @Override
    public void disprovedChop(ViolationChop chop) {
    }

    @Override
    public void disprovedAll() {
    }

    @Override
    public void addedNewEdges(Map<SDGEdge, StaticCGJavaMethod> edgesToMethods,
                              List<SDGEdge> edgesSorted, SDG sdg) {
        for (SDGEdge e : edgesToMethods.keySet()) {
            Collection<SDGNodeTuple> allFormalPairs = sdg.getAllFormalPairs(e.getSource(), e.getTarget());
            for (SDGNodeTuple t : allFormalPairs) {
                try {
                    String contract = javaForKeyCreator
                            .getMethodContractAndSetLoopInvariantAndSetMostGeneralContract(
                                    t.getFirstNode(), t.getSecondNode(), edgesToMethods.get(e),
                                    edgeToLoopInvariantTemplate, e, methodToMostGeneralContract);
                    formalTupleToContract.put(t, contract);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    ErrorLogger.logError(
                            "Error when trying to get the contract for " + t.toString(),
                            ErrorLogger.ErrorTypes.ERROR_PARSING_JOAK);
                }
            }

            ArrayList<String> loopInvariants = new ArrayList<>();
            for (int i = 0; i < edgesToMethods.get(e).getRelPosOfLoops().size(); ++i) {
                loopInvariants.add(null);
            }
            edgeToLoopInvariant.put(e, loopInvariants);
        }
    }
}
