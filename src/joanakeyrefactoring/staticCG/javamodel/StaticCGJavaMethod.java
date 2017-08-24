    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.staticCG.javamodel;

import java.util.Objects;
import java.util.Set;
import org.antlr.v4.runtime.misc.OrderedHashSet;

/**
 *
 * @author holger
 */
public class StaticCGJavaMethod {

    private StaticCGJavaClass containingClass;
    private String id;
    private String parameterTypes;
    private OrderedHashSet<StaticCGJavaMethod> calledMethods = new OrderedHashSet<>();
    private boolean isStatic;
    private String returnType;
    private Set<StaticCGJavaMethod> calledFunctionsRec;
    private String mostGeneralContract;
    
    public StaticCGJavaMethod(
            StaticCGJavaClass containingClass,
            String id, String parameterTypes,
            boolean isStatic, String returnType) {
        this.containingClass = containingClass;
        this.id = id;
        this.parameterTypes = parameterTypes;
        this.isStatic = isStatic;
        this.returnType = returnType;
    }

    public void setMostGeneralContract(String mostGeneralContract) {
        this.mostGeneralContract = mostGeneralContract;
    }

    public void setCalledFunctionsRec(Set<StaticCGJavaMethod> calledFunctionsRec) {
        this.calledFunctionsRec = calledFunctionsRec;
    }
    
    public boolean callsFunction(StaticCGJavaMethod m) {
        return calledFunctionsRec.contains(m);  
    }

    public Set<StaticCGJavaMethod> getCalledFunctionsRec() {
        return calledFunctionsRec;
    }
      

    public String getReturnType() {
        return returnType;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setIsStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public StaticCGJavaClass getContainingClass() {
        return containingClass;
    }

    public void setContainingClass(StaticCGJavaClass containingClass) {
        this.containingClass = containingClass;
    }

    public String getId() {
        return id;
    }

    public String getParameterWithoutPackage() {
        String[] seperatedByComma = parameterTypes.split(",");
        String created = "";
        for (int i = 0; i < seperatedByComma.length; ++i) {
            int lastIndexOfDot = seperatedByComma[i].lastIndexOf(".");
            created += seperatedByComma[i].substring(lastIndexOfDot + 1, seperatedByComma[i].length()) + ",";
        }
        if(!created.isEmpty()) {
            created = created.substring(0, created.length() - 1);
        }
        return created;
    }

    public void addCalledMethod(StaticCGJavaMethod m) {
        calledMethods.add(m);
    }

    public OrderedHashSet<StaticCGJavaMethod> getCalledMethods() {
        return calledMethods;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.containingClass);
        hash = 19 * hash + Objects.hashCode(this.id);
        hash = 19 * hash + Objects.hashCode(this.parameterTypes);
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
        final StaticCGJavaMethod other = (StaticCGJavaMethod) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.parameterTypes, other.parameterTypes)) {
            return false;
        }
        if (!Objects.equals(this.containingClass, other.containingClass)) {
            return false;
        }
        return true;
    }
    

}
