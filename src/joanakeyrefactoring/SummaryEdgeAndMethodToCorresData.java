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
import joanakeyrefactoring.javaforkeycreator.JavaForKeyCreator;
import joanakeyrefactoring.staticCG.JCallGraph;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author holger
 */
public class SummaryEdgeAndMethodToCorresData implements ViolationsWrapperListener {

    private HashMap<SDGNodeTuple, String> formalTupleToContract = new HashMap<>();
    private HashMap<SDGEdge, ArrayList<String>> edgeToLoopInvariant = new HashMap<>();
    private HashMap<SDGEdge, String> edgeToLoopInvariantTemplate = new HashMap<>();
    private HashMap<StaticCGJavaMethod, String> methodToMostGeneralContract = new HashMap<>();
    private JavaForKeyCreator javaForKeyCreator;

    public SummaryEdgeAndMethodToCorresData(
            Map<SDGEdge, StaticCGJavaMethod> edgesToMethods,
            SDG sdg,
            JavaForKeyCreator javaForKeyCreator) throws IOException {
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
        builder.append("\"formal_node_tuple_to_contract\" : [");
        formalTupleToContract.forEach((t, c) -> {
            builder.append("{");
            builder.append("\"in\" : " + t.getFirstNode().getId() + ", ");
            builder.append("\"out\" : " + t.getSecondNode().getId() + ", ");
            builder.append("\"contract\" : \"" + escapeContract(c) + "\"");
            builder.append("},\n");
        });
        if (builder.lastIndexOf(",") == builder.length() - 2) {
            builder.replace(builder.length() - 2, builder.length(), "");
        }
        builder.append("],");
        builder.append("\"edge_to_loop_invariant_template\" : [");
        edgeToLoopInvariantTemplate.forEach((e, s) -> {
            builder.append("{");
            builder.append("\"in\" : " + e.getSource().getId() + ", ");
            builder.append("\"out\" : " + e.getTarget().getId() + ", ");
            builder.append("\"invariant\" : \"" + escapeContract(s) + "\"");
            builder.append("},\n");
        });
        if (builder.lastIndexOf(",") == builder.length() - 2) {
            builder.replace(builder.length() - 2, builder.length(), "");
        }
        builder.append("],");
        builder.append("\"method_to_most_general_contract\" : [");
        methodToMostGeneralContract.forEach((m, c) -> {
            builder.append("{");
            builder.append("\"method\" : \""
                    + m.getContainingClass().getId()
                    + "/" + m.getId()
                    + "/" + m.getParameterWithoutPackage() + "\", ");
            builder.append("\"most_general_contract\" : \"" + escapeContract(c) + "\", ");
            builder.append("},\n");
        });
        if (builder.lastIndexOf(",") == builder.length() - 2) {
            builder.replace(builder.length() - 2, builder.length(), "");
        }
        builder.append("],");
        builder.append("\"edge_to_loop_invariant\" : [");
        edgeToLoopInvariant.forEach((e, i) -> {
            builder.append("{");
            builder.append("\"in\" : " + e.getSource().getId() + ", ");
            builder.append("\"out\" : " + e.getTarget().getId() + ", ");
            builder.append("\"loop_invariants\" : [");
            i.forEach((s) -> {
                if (s == null) {
                    builder.append("\"null\"");
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
        JSONArray currentJsonArray = jsonObject.getJSONArray("formal_node_tuple_to_contract");
        for (int i = 0; i < currentJsonArray.length(); ++i) {
            JSONObject currentJsonObj = currentJsonArray.getJSONObject(i);
            int idin = currentJsonObj.getInt("in");
            int idout = currentJsonObj.getInt("out");
            String contract
                    = addLinebreaksBackIntoContract(currentJsonObj.getString("contract"));
            SDGNode in = sdg.getNode(idin);
            SDGNode out = sdg.getNode(idout);
            SDGNodeTuple sdgNodeTuple = new SDGNodeTuple(in, out);
            created.formalTupleToContract.put(sdgNodeTuple, contract);
        }
        currentJsonArray = jsonObject.getJSONArray("edge_to_loop_invariant_template");
        for (int i = 0; i < currentJsonArray.length(); ++i) {
            JSONObject currentJsonObj = currentJsonArray.getJSONObject(i);
            int idin = currentJsonObj.getInt("in");
            int idout = currentJsonObj.getInt("out");
            String invariantTemplate = addLinebreaksBackIntoContract(
                    currentJsonObj.getString("invariant"));
            SDGNode in = sdg.getNode(idin);
            SDGNode out = sdg.getNode(idout);
            Set<SDGEdge> allEdges = sdg.getAllEdges(in, out);
            SDGEdge found = null;
            for (SDGEdge e : allEdges) {
                if (e.getKind() == SDGEdge.Kind.SUMMARY) {
                    found = e;
                    created.edgeToLoopInvariantTemplate.put(e, invariantTemplate);
                    break;
                }
            }
        }

        currentJsonArray = jsonObject.getJSONArray("method_to_most_general_contract");
        for (int i = 0; i < currentJsonArray.length(); ++i) {
            JSONObject currentJsonObj = currentJsonArray.getJSONObject(i);
            String methodDescr = currentJsonObj.getString("method");
            String[] split = methodDescr.split("/");
            String classId = split[0];
            String methodid = split[1];
            String argtypes;
            if (split.length == 3) {
                argtypes = split[2];
            } else {
                argtypes = "";
            }
            String mostGeneralContract = currentJsonObj.getString("most_general_contract");
            StaticCGJavaMethod method = callGraph.getMethodFor(classId, methodid, argtypes);
            created.methodToMostGeneralContract.put(method, mostGeneralContract);
        }

        currentJsonArray = jsonObject.getJSONArray("edge_to_loop_invariant");
        for (int i = 0; i < currentJsonArray.length(); ++i) {
            JSONObject currentJsonObj = currentJsonArray.getJSONObject(i);
            int inid = currentJsonObj.getInt("in");
            int outid = currentJsonObj.getInt("out");
            JSONArray loopInvArrs = currentJsonObj.getJSONArray("loop_invariants");
            ArrayList<String> loopInvariants = new ArrayList<>();
            for (int j = 0; j < loopInvArrs.length(); ++j) {
                String string = loopInvArrs.getString(j);
                if (string.equals("null")) {
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
    public void addedNewEdges(Map<SDGEdge, StaticCGJavaMethod> edgesToMethods, List<SDGEdge> edgesSorted, SDG sdg) {
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
                            "error when trying to get the contract for " + t.toString(),
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
