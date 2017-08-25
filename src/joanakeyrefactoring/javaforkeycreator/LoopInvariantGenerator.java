/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator;

/**
 *
 * @author holger
 */
public class LoopInvariantGenerator {

    /**
     * Creates loop invariants. Is not complete and only fills the determines
     * clause.
     *
     * @param descSinkG
     * @param descOtherParamsG
     * @param methodName
     * @param loopJava
     * @return loop invariant
     */
    public static String createLoopInvariant(String descSinkG,
            String descOtherParamsG, String methodName, String loopJava) {
        StringBuilder sb = new StringBuilder();
        String loopInvariant = "";

        String header = "\t/*\t@ loop_invariant " + "";
        String assignable = "\t \t @ assignable " + "";
        String descSink = descSinkG + "";
        String descOtherParams = descOtherParamsG + "";

        String determines = "\t \t @ determines " + descSink + " \\by "
                + descOtherParams + "; ";
        String decreases = "\t \t @ decreases " + "" + "*/";

        sb.append(header);
        sb.append(System.lineSeparator());
        if (assignable.length() > 11) {
            sb.append(assignable);
            sb.append(System.lineSeparator());
        }
        sb.append(determines);
        sb.append(System.lineSeparator());
        sb.append(decreases);
        loopInvariant = sb.toString();

        return loopInvariant;
    }

    public static String getTemplate() {
        String header = "/*@ loop_invariant \n";
        String decreases = "@ decreases */";
        return header + decreases;
    }
}
