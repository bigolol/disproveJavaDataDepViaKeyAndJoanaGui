/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.staticCG.javamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.antlr.v4.runtime.misc.OrderedHashSet;

/**
 *
 * @author holger
 */
public class StaticCGJavaClass {
    private String id;
    private OrderedHashSet<StaticCGJavaMethod> containedMethods = new OrderedHashSet<>();
    private OrderedHashSet<StaticCGJavaClass> referencedClasses = new OrderedHashSet<>();
   
    public StaticCGJavaClass(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void addReferencedClass(StaticCGJavaClass refC) {
        referencedClasses.add(refC);
    }
    
    public void addContainedMethod(StaticCGJavaMethod method) {
        containedMethods.add(method);
    }

    public OrderedHashSet<StaticCGJavaMethod> getContainedMethods() {
        return containedMethods;
    }

    public OrderedHashSet<StaticCGJavaClass> getReferencedClasses() {
        return referencedClasses;
    } 
    
    public String getOnlyClassName() {
        int packageDeclIndex = id.lastIndexOf(".");
        if(packageDeclIndex == -1) {
            return id;
        }
        return id.substring(packageDeclIndex + 1, id.length());
    }
    
    public String getPackageString() {
        int packageDeclIndex = id.lastIndexOf(".");
        if(packageDeclIndex == -1) {
            return null;
        }
        return id.substring(0, packageDeclIndex);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + id.hashCode();
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
        final StaticCGJavaClass other = (StaticCGJavaClass) obj;
        if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
    
    
    
}
