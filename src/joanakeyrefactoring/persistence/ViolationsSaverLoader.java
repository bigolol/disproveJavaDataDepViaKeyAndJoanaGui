/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.persistence;

import edu.kit.joana.api.sdg.SDGProgram;
import edu.kit.joana.ifc.sdg.core.SecurityNode;
import edu.kit.joana.ifc.sdg.core.violations.ClassifiedViolation;
import edu.kit.joana.ifc.sdg.core.violations.IViolation;
import edu.kit.joana.ifc.sdg.core.violations.paths.ViolationPath;
import edu.kit.joana.ifc.sdg.core.violations.paths.ViolationPathes;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author hklein
 */
public class ViolationsSaverLoader {

    public static String generateSaveString(Collection<? extends IViolation<SecurityNode>> violations) {
        StringBuilder created = new StringBuilder();

        created.append("\"violations\" : [");
        for (IViolation<SecurityNode> v : violations) {
            created.append("{");
            ClassifiedViolation classifiedViol = (ClassifiedViolation) v;
            int sourceId = classifiedViol.getSource().getId();
            created.append("\"source_id\" : ").append(sourceId).append(", ");
            int sinkId = classifiedViol.getSink().getId();
            created.append("\"sink_id\" : ").append(sinkId).append(", ");
            String attackerLevel = classifiedViol.getAttackerLevel();
            created.append("\"attacker_level\" : ").append("\"" + attackerLevel + "\"").append(", ");

            ViolationPathes violationPathes = classifiedViol.getViolationPathes();
            created.append("\"violpaths\" : [");
            for (ViolationPath p : violationPathes.getPathesList()) {
                created.append("{");
                LinkedList<SecurityNode> pathList = p.getPathList();
                created.append("\"node_ids\" : [");
                for (SecurityNode n : pathList) {
                    int secNodeId = n.getId();
                    created.append(secNodeId).append(", ");
                }
                if (created.lastIndexOf("[") != created.length() - 1) {
                    created.replace(created.length() - 2, created.length(), "");
                }
                created.append("]");
                created.append("},\n");
            }
            if (created.lastIndexOf("[") != created.length() - 1) {
                created.replace(created.length() - 2, created.length(), "");
            }
            created.append("]");
            created.append("},\n");
        }
        if (created.lastIndexOf("[") != created.length() - 1) {
            created.replace(created.length() - 2, created.length(), "");
        }
        created.append("]");
        return created.toString();
    }

    public static Collection<ClassifiedViolation> generateFromJSON(JSONArray violationsArr, SDG sdg) {
        Collection<ClassifiedViolation> created = new ArrayList<>();

        for (int i = 0; i < violationsArr.length(); ++i) {
            JSONObject currentViolJsonObj = violationsArr.getJSONObject(i);
            int currentSourceId = currentViolJsonObj.getInt("source_id");
            SecurityNode sourceNode = (SecurityNode) sdg.getNode(currentSourceId);
            int currentSinkId = currentViolJsonObj.getInt("sink_id");
            SecurityNode sinkNode = (SecurityNode) sdg.getNode(currentSinkId);
            String currentAttackerLvl = currentViolJsonObj.getString("attacker_level");
            
            JSONArray pathesArray = currentViolJsonObj.getJSONArray("violpaths");
            ViolationPathes pathes = new ViolationPathes();
            
            for(int j = 0; j < pathesArray.length(); ++j) {
                JSONArray nodeids = pathesArray.getJSONObject(j).getJSONArray("node_ids");
                LinkedList<SecurityNode> list = new LinkedList<>();
                for(int k = 0; k < nodeids.length(); ++k) {
                    SecurityNode secNode = (SecurityNode) sdg.getNode(nodeids.getInt(k));
                    list.add(secNode);
                }
                pathes.add(new ViolationPath(list));
            }
            
            ClassifiedViolation createdViolation = ClassifiedViolation.createViolation(sinkNode, sourceNode, pathes, currentAttackerLvl);
            created.add(createdViolation);
        }
        
        return created;
    }
}
