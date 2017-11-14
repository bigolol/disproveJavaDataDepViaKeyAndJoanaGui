/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeygui;

/**
 *
 * @author holger
 */
public class SinkOrSource {

    private static final String SEC_LEVEL = "SEC_LEVEL";
    private static final String PROGRAMPART = "PROGRAMPART";
    private static final String METHOD = "METHOD";
    private static final String PARAMPOS = "PARAMPOS";
    private static final String DESCR = "DESCR";

    private static final String FROM = "from";
    private static final String CALLS_TO_METHOD = "callsToMethod";
    private static final String PROGRAM_PART = "programPart";
    private static final String SECURITY_LEVEL_CAT = "securityLevel";
    private static final String METHOD_CAT = "method";
    private static final String PARAM_POS_CAT = "paramPos";
    private static final String DESCR_CAT = "description";

    private static final String THIS = "this";
    private static final String PARAM = "<param>";


    private String selectionMethod;
    private String selection;
    private int methodParam;
    private String securityLevel;

    public static SinkOrSource createMethod(String selection, String securityLevel) {
        SinkOrSource sinkOrSource = new SinkOrSource();
        sinkOrSource.selectionMethod = AddSourceDialogController.CALLS_TO_METHOD;
        sinkOrSource.securityLevel = securityLevel;
        return addSelectionAndParamForMethod(sinkOrSource, selection);
    }

    private static SinkOrSource addSelectionAndParamForMethod(SinkOrSource sinkOrSource,
                                                              String selection) {
        if (selection.endsWith(THIS)) {
            sinkOrSource.methodParam = 0;
            sinkOrSource.selection = selection.substring(0, selection.length() - THIS.length());
        } else {
            String[] split = selection.split(PARAM);
            int pos = Integer.valueOf(split[1].trim());
            sinkOrSource.methodParam = pos;
            sinkOrSource.selection = split[0];
        }
        return sinkOrSource;
    }

    public static SinkOrSource createProgramPart(String selection, String securityLevel) {
        SinkOrSource sinkOrSource = new SinkOrSource();
        sinkOrSource.selectionMethod = AddSourceDialogController.PROGRAM_PART;
        sinkOrSource.selection = selection;
        sinkOrSource.securityLevel = securityLevel;
        return sinkOrSource;
    }

    public String generateJson() {
        String templateStr =
                SECURITY_LEVEL_CAT + " : \"" + SEC_LEVEL + "\", " +
                DESCR_CAT + " : " + "{" + DESCR + "}";
        String descrTemplateMethod =
                FROM + " : \"" + CALLS_TO_METHOD + "\", " + METHOD_CAT +
                " : \"" + METHOD + "\", " + PARAM_POS_CAT + " : " + PARAMPOS;
        String descrTemplateProgramPart =
                FROM + " : \"" + PROGRAM_PART + "\", "
                + PROGRAM_PART + " : \"" + PROGRAMPART + "\"";
        if (selectionMethod.equals(AddSourceDialogController.PROGRAM_PART)) {
            String created = templateStr.replace(SEC_LEVEL, securityLevel);
            String desc = descrTemplateProgramPart.replace(PROGRAMPART, selection);
            return created.replace(DESCR, desc);
        } else if (selectionMethod.equals(AddSourceDialogController.CALLS_TO_METHOD)) {
            String created = templateStr.replace(SEC_LEVEL, securityLevel);
            String desc = descrTemplateMethod.replace(METHOD, selection);
            desc = desc.replace(PARAMPOS, String.valueOf(methodParam));
            return created.replace(DESCR, desc);
        }
        return "";
    }

    @Override
    public String toString() {
        return selectionMethod + ": " + selection;
    }   
    
}
