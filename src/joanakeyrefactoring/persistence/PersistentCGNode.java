/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.persistence;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author hklein
 */
public class PersistentCGNode {
    private PersistentIR persistentIR;
    private int uniqueId;    
    private int cgNodeId;
    
    public static PersistentCGNode generateFromJsonObj(JSONObject jsonObj) {
        PersistentCGNode node = new PersistentCGNode(jsonObj.getInt("id"), jsonObj.getInt("cg_node_id"));
        node.persistentIR = PersistentIR.generateFromJsonObj(jsonObj.getJSONObject("ir"), node.getUniqueId());
        return node;
    }

    public PersistentCGNode(int uniqueId) {
        this.uniqueId = uniqueId;
    }
        
    private PersistentCGNode(int id, int cgNodeId) {
        this.uniqueId = id;
        this.cgNodeId = cgNodeId;
    }
        
    public void createPersistentIR(CGNode n, List<LocalPointerKey> localPointerKeys) {
        persistentIR = new PersistentIR(localPointerKeys, n, uniqueId);
    }
    
    public String generateSaveString() {
        return "\"id\" : " + uniqueId + ", "
                + "\"cg_node_id\" :" + cgNodeId + ", " +
                "\"ir\" : {\n" + persistentIR.generateSaveString() + "\n}";
    }
        
    public PersistentIR getIR() {
        return persistentIR;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public int getCgNodeId() {
        return cgNodeId;
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
        final PersistentCGNode other = (PersistentCGNode) obj;
        if (this.uniqueId != other.uniqueId) {
            return false;
        }
        if(cgNodeId != other.cgNodeId) {
            return false;
        }
        if(!persistentIR.equals(other.persistentIR)) {
            return false;
        }
        return true;
    }

    public void setCgNodeId(int cgNodeId) {
        this.cgNodeId = cgNodeId;
    }
}
