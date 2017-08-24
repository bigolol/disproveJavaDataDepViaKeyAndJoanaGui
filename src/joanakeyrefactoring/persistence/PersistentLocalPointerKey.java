/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.persistence;

import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import java.util.List;
import java.util.Objects;
import org.json.JSONObject;

/**
 *
 * @author hklein
 */
public class PersistentLocalPointerKey {

    private int valueNumber;
    private PersistentCGNode persistentCGNode;
    private int id;

    public static PersistentLocalPointerKey generateFromJsonObj(JSONObject jsonObj, List<PersistentCGNode> cgNodes) {
        int nodeIndex = jsonObj.getInt("node");
        PersistentCGNode cgNode = cgNodes.get(nodeIndex);
        int valueNumber = jsonObj.getInt("value_number");
        int id = jsonObj.getInt("id");
        PersistentLocalPointerKey persistentLocalPointerKey = new PersistentLocalPointerKey();
        persistentLocalPointerKey.valueNumber = valueNumber;
        persistentLocalPointerKey.id = id;
        persistentLocalPointerKey.persistentCGNode = cgNode;
        return persistentLocalPointerKey;
    }

    private PersistentLocalPointerKey() {
    }

    public PersistentLocalPointerKey(LocalPointerKey localPointerKey, PersistentCGNode persistentCGNode, int uniqueId) {
        valueNumber = localPointerKey.getValueNumber();
        this.persistentCGNode = persistentCGNode;
        this.id = uniqueId;
    }

    public int getValueNumber() {
        return valueNumber;
    }

    public PersistentCGNode getNode() {
        return persistentCGNode;
    }

    public int getId() {
        return id;
    }

    public String generateSaveString() {
        return "\"id\" : " + id
                + ", \"value_number\" : " + String.valueOf(valueNumber)
                + ", \"node\" : " + persistentCGNode.getUniqueId();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
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
        final PersistentLocalPointerKey other = (PersistentLocalPointerKey) obj;
        if (this.valueNumber != other.valueNumber) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.persistentCGNode, other.persistentCGNode)) {
            return false;
        }
        return true;
    }

  
    
    
}
