/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.persistence;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ssa.IR;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author hklein
 */
public class PersistentIR {

    private Map<Integer, String> vnsToLocalNames = new HashMap<>();
    private int containingNodeId;

    public static PersistentIR generateFromJsonObj(JSONObject jsonObject, int nodeId) {
        PersistentIR persistentIR = new PersistentIR();
        persistentIR.containingNodeId = nodeId;
        
        Set<String> keySet = jsonObject.keySet();
        for(String k : keySet) {
            int i = Integer.valueOf(k);
            String corrName = jsonObject.getString(k);
            persistentIR.vnsToLocalNames.put(i, corrName);
        }
        
        return persistentIR;
    }

    private PersistentIR() {
    }

    public PersistentIR(List<LocalPointerKey> localPointerKeys, CGNode n, int containingNodeId) {
        this.containingNodeId = containingNodeId;
        IR ir = n.getIR();
        for (LocalPointerKey localPointerKey : localPointerKeys) {
            if (!localPointerKey.getNode().equals(n)) {
                continue;
            }
            String[] localNames = ir.getLocalNames(0, localPointerKey.getValueNumber());
            if (localNames != null && localNames.length != 0) {
                if (localNames[0] != null) {
                    vnsToLocalNames.put(localPointerKey.getValueNumber(), localNames[0]);
                }
            }
        }
    }

    public String generateSaveString() {
        StringBuilder sb = new StringBuilder();
        vnsToLocalNames.forEach((i, s) -> {
            sb.append("\"").append(i.toString()).append("\"").append(" : ").append("\"").append(s).append("\"").append(",\n");
        });
        if (sb.length() > 0) {
            sb.replace(sb.length() - 2, sb.length(), "");
        }
        return sb.toString();
    }

    public String getLocalName(int vn) {
        return vnsToLocalNames.get(vn);
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
        final PersistentIR other = (PersistentIR) obj;
        if (this.containingNodeId != other.containingNodeId) {
            return false;
        }
        for(Integer k : vnsToLocalNames.keySet()) {
            if(!vnsToLocalNames.get(k).equals(other.vnsToLocalNames.get(k))) {
                return false;
            }
        }
        return true;
    }
    
    
}
