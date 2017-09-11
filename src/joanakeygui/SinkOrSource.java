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

    private String selectionMethod;
    private String selection;
    private int methodParam;
    private String securityLevel;

    public static SinkOrSource createMethod(String selection, String securityLevel) {
        SinkOrSource sinkOrSource = new SinkOrSource();
        sinkOrSource.selectionMethod = "callsToMethod";
        sinkOrSource.securityLevel = securityLevel;
        return addSelectionAndParamForMethod(sinkOrSource, selection);
    }

    private static SinkOrSource addSelectionAndParamForMethod(SinkOrSource sinkOrSource, String selection) {
        if (selection.endsWith("this")) {
            sinkOrSource.methodParam = 0;
            sinkOrSource.selection = selection.substring(0, selection.length() - "this".length());
        } else {
            String[] split = selection.split("<param>");
            int pos = Integer.valueOf(split[1].trim());
            sinkOrSource.methodParam = pos;
            sinkOrSource.selection = split[0];
        }
        return sinkOrSource;
    }

    public static SinkOrSource createProgramPart(String selection, String securityLevel) {
        SinkOrSource sinkOrSource = new SinkOrSource();
        sinkOrSource.selectionMethod = "programPart";
        sinkOrSource.selection = selection;
        sinkOrSource.securityLevel = securityLevel;
        return sinkOrSource;
    }

    public String generateJson() {
        String templateStr = "securityLevel : \"SEC_LEVEL\", description : {DESCR}";
        String descrTemplateMethod = "from : \"callsToMethod\", method : \"METHOD\", paramPos : PARAMPOS";
        String descrTemplateProgramPart = "from : \"programPart\", programPart : \"PROGRAMPART\"";
        if (selectionMethod.equals("programPart")) {
            String created = templateStr.replace("SEC_LEVEL", securityLevel);
            String desc = descrTemplateProgramPart.replace("PROGRAMPART", selection);
            return created.replace("DESCR", desc);
        } else if (selectionMethod.equals("callsToMethod")) {
            String created = templateStr.replace("SEC_LEVEL", securityLevel);
            String desc = descrTemplateMethod.replace("METHOD", selection);
            desc = desc.replace("PARAMPOS", String.valueOf(methodParam));
            return created.replace("DESCR", desc);
        }
        return "";
    }

    @Override
    public String toString() {
        return selectionMethod + ": " + selection;
    }   
    
}
