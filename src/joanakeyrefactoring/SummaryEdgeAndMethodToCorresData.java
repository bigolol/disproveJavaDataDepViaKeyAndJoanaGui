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
import joanakeyrefactoring.staticCG.JCallGraph;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author holger
 */
public class SummaryEdgeAndMethodToCorresData {

    private HashMap<SDGNodeTuple, String> formalTupleToContract = new HashMap<>();
    private HashMap<SDGEdge, ArrayList<String>> edgeToLoopInvariant = new HashMap<>();
    private HashMap<SDGEdge, String> edgeToLoopInvariantTemplate = new HashMap<>();
    private HashMap<StaticCGJavaMethod, String> methodToMostGeneralContract = new HashMap<>();

    public SummaryEdgeAndMethodToCorresData(
            Map<SDGEdge, StaticCGJavaMethod> edgesToMethods,
            SDG sdg,
            JavaForKeyCreator javaForKeyCreator) throws IOException {
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
        for(int i = 0; i < contract.length(); ++i) {
            if(contract.charAt(i) == '\\') {
                contract = contract.substring(0, i) + '\\' + '\\' + contract.substring(i + 1, contract.length() - 1);
                i+=2;
            } else if (contract.charAt(i) == '\n') {
                contract = contract.substring(0, i - 1) + contract.substring(i + 1, contract.length() - 1);
            }
        }
        return contract;
    }
    
    public String generateSaveString() {
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
            builder.append("\"most_general_contract\" : \"" + escapeContract(c) + "\"");
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
    
    private SummaryEdgeAndMethodToCorresData(){}

    public static SummaryEdgeAndMethodToCorresData 
        generateFromjson(JSONObject jsonObject, JCallGraph callGraph, SDG sdg) {
            SummaryEdgeAndMethodToCorresData created = new SummaryEdgeAndMethodToCorresData();
        JSONArray nodetupletocontract = jsonObject.getJSONArray("formal_node_tuple_to_contract");
        for(int i = 0; i < nodetupletocontract.length(); ++i) {
            JSONObject currentTupleContract = nodetupletocontract.getJSONObject(i);
            int idin = currentTupleContract.getInt("in");
            int idout = currentTupleContract.getInt("out");
            String contract = currentTupleContract.getString("contract");
            int j = 0;
        }
        
        return created;
    }
}
