/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import joanakeyrefactoring.AutomationHelper;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public class KeYFileCreator {

    private final static String ENCODING = "UTF-8";
    private final static String INIT = "<init>";
    private final static String JAVA_PROFILE = "Java Profile";
    private final static String METHODNAME = "METHODNAME";
    private final static String CLASSNAME = "CLASSNAME";
    private final static String INT_ARR_JAVA = "int\\[\\]";
    private final static String INT_ARR_KeY = "\\[I";
    private final static String BYTE_ARR_JAVA = "byte\\[\\]";
    private final static String BYTE_ARR_KeY = "\\[B";

    /**
     * Creates the Information flow Proof Obligation for KeY.
     *
     * @param javaFile
     * @param method
     */
    public static void createKeYFileIF(StaticCGJavaMethod method,
                                       String pathToSave) throws IOException {
        File proofObFile = new File(pathToSave + AutomationHelper.PO_NAME_IF);
        if (!proofObFile.exists()) {
            proofObFile.createNewFile();
        }

        String methodnameKey = getMethodnameKey(method);

        final String settingsStr = AutomationHelper.getSettings();
        final String javaSourceStr = "./";
        final String proofObligationTemplateString
                = "#Proof Obligation Settings\n"
                + "name=CLASSNAME[CLASSNAME\\\\:\\\\:" + METHODNAME
                + "].Non-interference contract.0\n"
                + "contract=CLASSNAME[CLASSNAME\\\\:\\\\:" + METHODNAME
                + "].Non-interference contract.0\n"
                + "class=de.uka.ilkd.key.informationflow.po.InfFlowContractPO\n";
        final String proofObligationString = proofObligationTemplateString
                .replaceAll(METHODNAME, methodnameKey)
                .replaceAll("CLASSNAME", method.getContainingClass().getId());

        generateKeyFileFrom(JAVA_PROFILE, settingsStr, javaSourceStr, proofObligationString, proofObFile);
    }

    public static void createKeYFileFunctional(StaticCGJavaMethod method,
                                               String pathToSave) throws IOException {
        File proofObFile = new File(pathToSave + "/" + AutomationHelper.PO_NAME_FUNCTIONAL);
        if (!proofObFile.exists()) {
            proofObFile.createNewFile();
        }

        final String settingsStr = AutomationHelper.getSettings();
        final String javaSourceStr = "./";

        String methodnameKey = getMethodnameKey(method);

        final String proofObligationTemplateString
                = "#Proof Obligation Settings\n"
                + "name=" + CLASSNAME + "[" + CLASSNAME
                + "\\\\:\\\\:" + METHODNAME
                + "].JML operation contract.0\n"
                + "contract=" + CLASSNAME + "[" + CLASSNAME
                + "\\\\:\\\\:" + METHODNAME
                + "].JML operation contract.0\n"
                + "class=de.uka.ilkd.key.proof.init.FunctionalOperationContractPO\n";
        final String proofObligationString = proofObligationTemplateString
                .replaceAll(METHODNAME, methodnameKey)
                .replaceAll(CLASSNAME, method.getContainingClass().getId());

        generateKeyFileFrom(JAVA_PROFILE, settingsStr, javaSourceStr, proofObligationString, proofObFile);
    }

    private static String getMethodnameKey(StaticCGJavaMethod method) {
        String params =
                method.getParameterWithoutPackage()
                .replaceAll(INT_ARR_JAVA, INT_ARR_KeY)
                .replaceAll(BYTE_ARR_JAVA, BYTE_ARR_KeY);
        if (method.getId().equals(INIT)) {
            return method.getContainingClass().getOnlyClassName() + "(" + params + ")";
        } else {
            return method.getId() + "(" + params + ")";
        }
    }

    private static void generateKeyFileFrom(String profileString, String settingsString,
                                            String javaSourceString, String proofObligationString,
                                            File f) throws IOException {
        String profileTempStr = "\\profile PROFILE;\n";
        String javaSourceTempStr = "\\javaSource JAVASRC;\n";
        String proofOblTempStr = "\\proofObligation PROOFOBL;\n";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(profileTempStr.replace("PROFILE",
                                                    surroundWithApos(profileString)));
        stringBuilder.append('\n');
        stringBuilder.append(settingsString);
        stringBuilder.append('\n');
        stringBuilder.append(javaSourceTempStr.replace("JAVASRC",
                                                       surroundWithApos(javaSourceString)));
        stringBuilder.append('\n');
        stringBuilder.append(proofOblTempStr.replace("PROOFOBL",
                                                     surroundWithApos(proofObligationString)));

        PrintWriter writer = new PrintWriter(f, ENCODING);
        writer.print(stringBuilder.toString());
        writer.close();
    }

    private static String surroundWithApos(String s) {
        return "\"" + s + "\"";
    }

}
