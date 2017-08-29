/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring;

import disproveviakeyandjoanagui.ViolationsWrapperListener;
import edu.kit.joana.api.IFCAnalysis;
import edu.kit.joana.api.sdg.SDGMethod;
import edu.kit.joana.ifc.sdg.core.SecurityNode;
import edu.kit.joana.ifc.sdg.core.violations.ClassifiedViolation;
import edu.kit.joana.ifc.sdg.core.violations.IViolation;
import edu.kit.joana.ifc.sdg.core.violations.paths.ViolationPath;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import edu.kit.joana.ifc.sdg.util.JavaType;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import joanakeyrefactoring.persistence.ViolationsSaverLoader;
import joanakeyrefactoring.staticCG.JCallGraph;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author holgerklein
 */
public class ViolationsWrapper {

    private Collection<? extends IViolation<SecurityNode>> uncheckedViolations;
    private SDG sdg;
    private Collection<ViolationChop> violationChops = new ArrayList<>();
    private HashSet<SDGEdge> checkedEdges = new HashSet<>();
    private HashSet<SDGEdge> edgesToCheck = new HashSet<>();
    private Map<SDGEdge, ArrayList<ViolationChop>> summaryEdgesAndContainingChops = new HashMap<>();
    private Map<SDGEdge, StaticCGJavaMethod> summaryEdgesAndCorresJavaMethods = new HashMap<>();
    private List<SDGEdge> sortedEdgesToCheck = new ArrayList<>();
    private JCallGraph callGraph = new JCallGraph();
    private IFCAnalysis ana;
    private List<String> keyCompatibleJavaFeatures = new ArrayList<>();
    private List<ViolationsWrapperListener> listener = new ArrayList<>();

    public ViolationsWrapper(
            Collection<? extends IViolation<SecurityNode>> violations,
            SDG sdg, IFCAnalysis ana, JCallGraph callGraph) throws IOException {
        this.uncheckedViolations = violations;
        this.sdg = sdg;
        this.callGraph = callGraph;
        this.ana = ana;

        InputStream is = new FileInputStream("otherdata/JAVALANG.txt");
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        while (line != null) {
            keyCompatibleJavaFeatures.add(line);
            line = buf.readLine();
        }
        buf.close();
        prepareNextSummaryEdges();
    }

    public void addListener(ViolationsWrapperListener listener) {
        this.listener.add(listener);
    }

    public String generateSaveString() {
        StringBuilder created = new StringBuilder();
        created.append("{").append(System.lineSeparator());
        int lengthOfLineSep = System.lineSeparator().length();

        created.append(ViolationsSaverLoader.generateSaveString(uncheckedViolations));
        created.append(",\n");

        created.append("\"chops\" : [").append(System.lineSeparator());
        for (ViolationChop vc : violationChops) {
            created.append(vc.generateSaveString())
                    .append(",")
                    .append(System.lineSeparator());
        }
        int length = created.length();
        int lastIndexOf = created.lastIndexOf("[");
        if (created.lastIndexOf("[") != created.length() - lengthOfLineSep - 1) {
            created.replace(created.length() - lengthOfLineSep - 1, created.length(), "");
        }
        created.append("],").append(System.lineSeparator());

        created.append("\"summary_edges_sorted\" : [").append(System.lineSeparator());
        for (SDGEdge e : sortedEdgesToCheck) {
            created.append("{");

            int srcId = sdg.getEdgeSource(e).getId();
            int sinkId = sdg.getEdgeTarget(e).getId();

            if (srcId == 14724 && sinkId == 49581) {
                SDGEdge foundEdge = sdg.getEdge(sdg.getNode(srcId), sdg.getNode(sinkId));
            }

            SDGEdge foundEdge = sdg.getEdge(sdg.getNode(srcId), sdg.getNode(sinkId));

            created.append("\"src\" : ").append(srcId);
            created.append(", \"sink\" : ").append(sinkId);
            created.append("},").append(System.lineSeparator());
        }
        if (created.lastIndexOf("[") != created.length() - lengthOfLineSep - 1) {
            created.replace(created.length() - lengthOfLineSep - 1, created.length(), "");
        }
        created.append("],").append(System.lineSeparator());

        created.append("\"summary_edges_methods\": [").append(System.lineSeparator());
        for (Map.Entry<SDGEdge, StaticCGJavaMethod> entry : summaryEdgesAndCorresJavaMethods.entrySet()) {
            int edgeId = sortedEdgesToCheck.indexOf(entry.getKey());
            created.append("{");
            created.append("\"id\" :").append(edgeId);
            created.append(", \"class_name\" : ")
                    .append("\"").append(entry.getValue().getContainingClass().getId())
                    .append("\"");
            created.append(", \"method_name\" : ")
                    .append("\"").append(entry.getValue().getId()).append("\"");
            created.append(", \"arg_string\" : ")
                    .append("\"").append(entry.getValue().getParameterWithoutPackage())
                    .append("\"");
            created.append("},").append(System.lineSeparator());
        }
        if (created.lastIndexOf("[") != created.length() - lengthOfLineSep - 1) {
            created.replace(created.length() - lengthOfLineSep - 1, created.length(), "");
        }
        created.append("],");

        created.append("\"summary_edges_chops\" : [").append(System.lineSeparator());
        for (Map.Entry<SDGEdge, ArrayList<ViolationChop>> entry : summaryEdgesAndContainingChops.entrySet()) {
            int edgeId = sortedEdgesToCheck.indexOf(entry.getKey());
            created.append("{");
            created.append("\"pos\" :").append(edgeId);
            created.append(", \"chops\" : [").append(System.lineSeparator());
            for (ViolationChop vc : entry.getValue()) {
                created.append("{");
                created.append("\"src\" : ").append(vc.getViolationSource().getId());
                created.append(", \"sink\" : ").append(vc.getViolationSink().getId());
                created.append("},").append(System.lineSeparator());
            }
            if (created.lastIndexOf("[") != created.length() - lengthOfLineSep - 1) {
                created.replace(created.length() - lengthOfLineSep - 1, created.length(), "");
            }
            created.append("]");

            created.append("},").append(System.lineSeparator());
        }
        if (created.lastIndexOf("[") != created.length() - lengthOfLineSep - 1) {
            created.replace(created.length() - lengthOfLineSep - 1, created.length(), "");
        }
        created.append("]").append(System.lineSeparator());
        created.append("}");
        return created.toString();
    }

    private ViolationsWrapper() {
    }

    public static ViolationsWrapper generateFromJsonObj(
            JSONObject jSONObject, SDG sdg, JCallGraph callGraph) {
        ViolationsWrapper created = new ViolationsWrapper();

        JSONArray violArr = jSONObject.getJSONArray("violations");
        created.uncheckedViolations = ViolationsSaverLoader.generateFromJSON(violArr, sdg);

        JSONArray chopArr = jSONObject.getJSONArray("chops");
        for (int i = 0; i < chopArr.length(); ++i) {
            created.violationChops.add(ViolationChop.generateFromJsonObj(
                    chopArr.getJSONObject(i), sdg));
        }

        JSONArray summaryEdgesSortedArr = jSONObject.getJSONArray("summary_edges_sorted");

        for (int i = 0; i < summaryEdgesSortedArr.length(); ++i) {
            int srcId = summaryEdgesSortedArr.getJSONObject(i).getInt("src");
            int sinkId = summaryEdgesSortedArr.getJSONObject(i).getInt("sink");

            SDGEdge edge = sdg.getEdge(sdg.getNode(srcId), sdg.getNode(sinkId));
            Set<SDGEdge> allEdges = sdg.getAllEdges(sdg.getNode(srcId), sdg.getNode(sinkId));
            if (allEdges.size() == 0) {
                System.out.println("missing sue : src " + srcId + " sink " + sinkId);
            }
            for (SDGEdge e : allEdges) {
                if (e.getKind() == SDGEdge.Kind.SUMMARY) {
                    created.sortedEdgesToCheck.add(e);
                    break;
                }
            }
        }

        JSONArray edgesToMethodsArr = jSONObject.getJSONArray("summary_edges_methods");

        for (int i = 0; i < edgesToMethodsArr.length(); ++i) {
            JSONObject currentJsonObj = edgesToMethodsArr.getJSONObject(i);
            int posInSorted = currentJsonObj.getInt("id");
            String classname = currentJsonObj.getString("class_name");
            String methodName = currentJsonObj.getString("method_name");
            String args = currentJsonObj.getString("arg_string");
            StaticCGJavaMethod method = callGraph.getMethodFor(classname, methodName, args);
            created.summaryEdgesAndCorresJavaMethods.put(
                    created.sortedEdgesToCheck.get(posInSorted),
                    method
            );
        }

        JSONArray jsonChopArr = jSONObject.getJSONArray("summary_edges_chops");

        for (int i = 0; i < jsonChopArr.length(); ++i) {
            JSONObject currentJsonObj = jsonChopArr.getJSONObject(i);
            int posInSorted = currentJsonObj.getInt("pos");
            JSONArray currentChopJsonArr = currentJsonObj.getJSONArray("chops");
            ArrayList<ViolationChop> chopList = new ArrayList<>();
            for (int j = 0; j < currentChopJsonArr.length(); ++j) {
                int sourceId = currentChopJsonArr.getJSONObject(j).getInt("src");
                int sinkId = currentChopJsonArr.getJSONObject(j).getInt("sink");
                SDGNode sourceNode = sdg.getNode(sourceId);
                SDGNode sinkNode = sdg.getNode(sinkId);
                for (ViolationChop vc : created.violationChops) {
                    if (vc.getViolationSource().equals(sourceNode)
                            && vc.getViolationSink().equals(sinkNode)) {
                        chopList.add(vc);
                        break;
                    }
                }
            }
            created.summaryEdgesAndContainingChops.put(
                    created.sortedEdgesToCheck.get(posInSorted), chopList);
        }

        created.sdg = sdg;
        created.callGraph = callGraph;
        return created;
    }

    public ViolationChop getChop() {
        return violationChops.iterator().next();
    }

    private void prepareNextSummaryEdges() {
        Collection<? extends IViolation<SecurityNode>> nextViolationsToHandle = getNextViolationsToHandle();

        nextViolationsToHandle.forEach((v) -> {
            violationChops.add(createViolationChop(v, sdg));
        });
        putEdgesInSet();
        findCGMethodsForSummaryEdgesIfKeyCompatible();
        putEdgesAndChopsInMap();

        sortedEdgesToCheck = new ArrayList<>();
        for (SDGEdge e : summaryEdgesAndCorresJavaMethods.keySet()) {
            sortedEdgesToCheck.add(e);
        }
        for (int i = 0; i < 10; ++i) {
            try {
                sortedEdgesToCheck.sort(new SummaryEdgeComparator(this));
                break;
            } catch (Exception e) {
            }
        }

        violationChops.forEach((vc) -> {
            listener.forEach((l) -> {
                l.parsedChop(vc);
            });
        });
        listener.forEach((l) -> {
            l.addedNewEdges(summaryEdgesAndCorresJavaMethods, sortedEdgesToCheck, sdg);
        });
    }

    private void putEdgesInSet() {
        for (ViolationChop vc : violationChops) {
            Collection<SDGEdge> summaryEdges = vc.getSummaryEdges();
            for (SDGEdge summaryEdge : summaryEdges) {
                edgesToCheck.add(summaryEdge);
            }
        }
    }

    private Collection<IViolation<SecurityNode>> getNextViolationsToHandle() {
        int amt_viols = 1;
        List<IViolation<SecurityNode>> created = new ArrayList<>();
        int i = 0;
        for (IViolation<SecurityNode> v : uncheckedViolations) {
            if (i == amt_viols) {
                break;
            }
            created.add(v);
            ++i;
        }
        for (IViolation<SecurityNode> v : created) {
            uncheckedViolations.remove(v);
        }
        return created;
    }

    private void findCGMethodsForSummaryEdgesIfKeyCompatible() {
        for (SDGEdge summaryEdge : edgesToCheck) {
            Collection<SDGNodeTuple> allFormalPairs = sdg.getAllFormalPairs(summaryEdge.getSource(), summaryEdge.getTarget());
            SDGNodeTuple firstPair = allFormalPairs.iterator().next();
            SDGNode methodNode = sdg.getEntry(firstPair.getFirstNode());
            String bytecodeMethod = methodNode.getBytecodeMethod();
            SDGMethod method = ana.getProgram().getMethod(bytecodeMethod);
            List<JavaType> argumentTypes = method.getSignature().getArgumentTypes();
            String types = "";
            for (JavaType currType : argumentTypes) {
                String toHRString = currType.toHRString();
                int lastIndexOfDot = toHRString.lastIndexOf(".");
                toHRString = toHRString.substring(lastIndexOfDot + 1, toHRString.length());
                types += toHRString + ",";
            }
            if (!types.isEmpty()) {
                types = types.substring(0, types.length() - 1);
            }
            String methodName = method.getSignature().getMethodName();
            String fullyQualifiedMethodName = method.getSignature().getFullyQualifiedMethodName();
            int classNameEndIndex = fullyQualifiedMethodName.lastIndexOf(".");
            String className = fullyQualifiedMethodName.substring(0, classNameEndIndex);
            StaticCGJavaMethod callGraphMethod = callGraph.getMethodFor(className, methodName, types);
            if (isIndepOfLibs(callGraphMethod)) {
                summaryEdgesAndCorresJavaMethods.put(summaryEdge, callGraphMethod);
            }
        }
        edgesToCheck.clear();
    }

    public StaticCGJavaMethod getMethodCorresToSummaryEdge(SDGEdge e) {
        return summaryEdgesAndCorresJavaMethods.get(e);
    }

    private void putEdgesAndChopsInMap() {
        summaryEdgesAndContainingChops = new HashMap<>();
        for (ViolationChop vc : violationChops) {
            Collection<SDGEdge> summaryEdges = vc.getSummaryEdges();
            for (SDGEdge summaryEdge : summaryEdges) {
                if (summaryEdgesAndCorresJavaMethods.containsKey(summaryEdge)) {
                    if (summaryEdgesAndContainingChops.containsKey(summaryEdge)) {
                        summaryEdgesAndContainingChops.get(summaryEdge).add(vc);
                    } else {
                        ArrayList<ViolationChop> vcList = new ArrayList<>();
                        vcList.add(vc);
                        summaryEdgesAndContainingChops.put(summaryEdge, vcList);
                    }
                }
            }
        }
    }

    private ViolationChop createViolationChop(IViolation<SecurityNode> violationNode, SDG sdg) {
        ViolationPath violationPath = getViolationPath(violationNode);
        LinkedList<SecurityNode> violationPathList = violationPath.getPathList();
        SDGNode violationSource = violationPathList.get(0);
        SDGNode violationSink = violationPathList.get(1);
        return new ViolationChop(violationSource, violationSink, sdg);
    }

    private ViolationPath getViolationPath(IViolation<SecurityNode> v) {
        return ((ClassifiedViolation) v).getChops().iterator().next().getViolationPathes().getPathesList().get(0);
    }

    public boolean allDisproved() {
        for (ViolationChop vc : violationChops) {
            if (!vc.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean allCheckedOrDisproved() {
        for (ViolationChop vc : violationChops) {
            if (!vc.isEmpty()) {
                for (SDGEdge se : vc.getSummaryEdges()) {
                    if (!checkedEdges.contains(se)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void removeEdge(SDGEdge e) {
        sdg.removeEdge(e);
        sortedEdgesToCheck.remove(e);
        listener.forEach((l) -> {
            l.disprovedEdge(e);
        });
        summaryEdgesAndContainingChops.get(e).forEach((vc) -> {
            vc.findSummaryEdges(sdg);
            if (vc.isEmpty()) {
                violationChops.remove(vc);
                listener.forEach((l) -> {
                    l.disprovedChop(vc);
                });
            }
        });
        summaryEdgesAndContainingChops.remove(e);
        summaryEdgesAndCorresJavaMethods.remove(e);
        if (sortedEdgesToCheck.isEmpty()) {
            prepareNextSummaryEdges();
        }
        if (sortedEdgesToCheck.isEmpty() && violationChops.isEmpty() && uncheckedViolations.isEmpty()) {
            listener.forEach((l) -> {
                l.disprovedAll();
            });
        }
    }

    public void checkedEdge(SDGEdge e) {
        checkedEdges.add(e);
        sortedEdgesToCheck.remove(e);
        if (sortedEdgesToCheck.isEmpty()) {
            prepareNextSummaryEdges();
        }
    }

    public SDGEdge nextSummaryEdge() {
        return sortedEdgesToCheck.get(0);
    }

    ArrayList<ViolationChop> getChopsContaining(SDGEdge e) {
        return summaryEdgesAndContainingChops.get(e);
    }

    private boolean isIndepOfLibs(StaticCGJavaMethod m) {
        String packageName = callGraph.getPackageName();
        try {
            String classPackageName = m.getContainingClass().getPackageString();
            if (!classPackageName.startsWith(packageName)) {
                return false;
            }
            Set<StaticCGJavaMethod> allMethodsCalledByMethodRec = m.getCalledFunctionsRec();
            for (StaticCGJavaMethod calledM : allMethodsCalledByMethodRec) {
                if (!calledM.getContainingClass().getPackageString().startsWith(packageName)) {
                    if (!isKeyFeature(calledM)) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
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
        final ViolationsWrapper other = (ViolationsWrapper) obj;
        if (sortedEdgesToCheck.size() != other.sortedEdgesToCheck.size()) {
            return false;
        }
        for (int i = 0; i < sortedEdgesToCheck.size(); ++i) {
            if (!sortedEdgesToCheck.get(i).equals(other.sortedEdgesToCheck.get(i))) {
                return false;
            }
        }
        for (SDGEdge e : summaryEdgesAndCorresJavaMethods.keySet()) {
            if (!summaryEdgesAndCorresJavaMethods.get(e).equals(
                    other.summaryEdgesAndCorresJavaMethods.get(e))) {
                return false;
            }

        }

        return true;
    }

    private boolean isKeyFeature(StaticCGJavaMethod calledM) {
        String onlyClassName = calledM.getContainingClass().getOnlyClassName();
        for (String compatible : keyCompatibleJavaFeatures) {
            if (compatible.endsWith(onlyClassName)) {
                return true;
            }
        }
        return false;
    }

    public Collection<? extends IViolation<SecurityNode>> getUncheckedViolations() {
        return uncheckedViolations;
    }

    public Map<SDGEdge, ArrayList<ViolationChop>> getSummaryEdgesAndContainingChops() {
        return summaryEdgesAndContainingChops;
    }

    public Map<SDGEdge, StaticCGJavaMethod> getSummaryEdgesAndCorresJavaMethods() {
        return summaryEdgesAndCorresJavaMethods;
    }

    public SDG getSdg() {
        return sdg;
    }

}
