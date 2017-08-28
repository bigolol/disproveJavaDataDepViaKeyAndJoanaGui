/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.staticCG.javamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import joanakeyrefactoring.javaforkeycreator.LoopInvariantGenerator;
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
    private String mostGeneralContract = "";
    private ArrayList<Integer> relPosOfLoops = new ArrayList<>();
    private String methodBody = "";

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
        if (!created.isEmpty()) {
            created = created.substring(0, created.length() - 1);
        }
        return created;
    }

    public String getSaveString() {
        String template
                = "{ \"parameter_types\" : \"ARGS\","
                + "\"id\" : \"ID\","
                + "\"is_static\" : STATIC,"
                + "\"return_type\" : \"RETURNTYPE\","
                + "\"containing_class_id\" : \"CONTAININGCLASS\","
                + "\"most_general_contract\" : \"MOSTGENERALCONTRACT\","
                + "\"loop_lines\" : [LOOPS],"
                + "\"method_body\" : \"METHODBODY\","
                + "\"called_methods_rec\" : [CALLEDMETHODSREC]}";
        template = template.replace("ARGS", parameterTypes);
        template = template.replace("ID", id);
        template = template.replace("STATIC", String.valueOf(isStatic));
        template = template.replace("CONTAININGCLASS", containingClass.getId());
        template = template.replace("MOSTGENERALCONTRACT", mostGeneralContract);
        template = template.replace("METHODBODY", methodBody);
        template = template.replace("RETURNTYPE", returnType);

        //loop lines
        StringBuilder loops = new StringBuilder();
        for (int pos : relPosOfLoops) {
            loops.append(relPosOfLoops + ", ");
        }
        if (loops.lastIndexOf(",") != -1) {
            loops.replace(loops.length() - 2, loops.length(), "");
        }
        template = template.replace("LOOPS", loops.toString());
        //other methods
        StringBuilder calledmethodsrecBuilder = new StringBuilder();
        for (StaticCGJavaMethod cm : calledFunctionsRec) {
            calledmethodsrecBuilder.append("\"" + cm.containingClass.getId() + "/" + cm.getId() + "/" + cm.parameterTypes + "\", ");
        }
        //remove last comma
        if (calledmethodsrecBuilder.lastIndexOf(",") != -1) {
            calledmethodsrecBuilder.replace(calledmethodsrecBuilder.length() - 2, calledmethodsrecBuilder.length(), "");
        }
        template = template.replace("CALLEDMETHODSREC", calledmethodsrecBuilder.toString());
        return template;
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

    @Override
    public String toString() {
        return containingClass.getId() + "." + getId() + "(" + getParameterWithoutPackage() + ") -> " + returnType;
    }

    public void addRelativeLoopLinePos(int pos) {
        relPosOfLoops.add(pos);
    }

    public String getMethodBody() {
        return methodBody;
    }

    public ArrayList<Integer> getRelPosOfLoops() {
        return relPosOfLoops;
    }

    public void setMethodBody(String methodBody) {
        this.methodBody = methodBody;
    }


    public void getMethodParamName(int p_number) {

    }
}
