/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator;

import joanakeyrefactoring.javaforkeycreator.javatokeypipeline.CopyKeyCompatibleListener;
import edu.kit.joana.api.IFCAnalysis;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import joanakeyrefactoring.CustomListener.GetMethodBodyListener;
import joanakeyrefactoring.StateSaver;
import joanakeyrefactoring.staticCG.JCallGraph;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaClass;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public class JavaForKeyCreator {

    private String pathToJavaSource;
    private String pathToTestJava = "proofObs/proofs";
    private JCallGraph callGraph;
    private SDG sdg;
    private StateSaver stateSaver;
    private JavaProjectCopyHandler javaProjectCopyHandler;
    private CopyKeyCompatibleListener keyCompatibleListener;

    private GetMethodBodyListener methodBodyListener = new GetMethodBodyListener();

    public JavaForKeyCreator(String pathToJavaSource, JCallGraph callGraph, SDG sdg, StateSaver stateSaver) {
        this.pathToJavaSource = pathToJavaSource;
        this.callGraph = callGraph;
        this.sdg = sdg;
        this.stateSaver = stateSaver;
    }

    public static String getPathToJavaClassFile(String pathToSource, StaticCGJavaClass javaClass) {
        return pathToSource + JavaProjectCopyHandler.getRelPathForJavaClass(javaClass)
                + javaClass.getOnlyClassName() + ".java";
    }

    public String generateJavaForFormalNodeTuple(
            SDGNodeTuple formalNodeTuple,
            StaticCGJavaMethod methodCorresToSE) throws IOException {
        this.keyCompatibleListener = new CopyKeyCompatibleListener(callGraph.getPackageName());

        SDGNode formalInNode = formalNodeTuple.getFirstNode();
        StaticCGJavaClass containingClass = methodCorresToSE.getContainingClass();
        String relPathForJavaClass
                = JavaProjectCopyHandler.getRelPathForJavaClass(containingClass);
        File javaClassFile = new File(pathToJavaSource + relPathForJavaClass + containingClass.getOnlyClassName() + ".java");

        if (!javaClassFile.exists()) { //it is a library class since it doesnt exist in the project
            throw new FileNotFoundException();
        }

        String contents = new String(Files.readAllBytes(javaClassFile.toPath()));

        Map<StaticCGJavaClass, Set<StaticCGJavaMethod>> allNecessaryClasses = callGraph.getAllNecessaryClasses(methodCorresToSE);

        String keyCompatibleContents = keyCompatibleListener.transformCode(
                contents, allNecessaryClasses.get(methodCorresToSE.getContainingClass()));

        methodBodyListener.parseFile(keyCompatibleContents, methodCorresToSE);

        javaProjectCopyHandler = new JavaProjectCopyHandler(pathToJavaSource, pathToTestJava, keyCompatibleListener);
        javaProjectCopyHandler.copyClasses(allNecessaryClasses);
        
        String descriptionForKey = getMethodContractFor(formalInNode, formalNodeTuple.getSecondNode(), methodCorresToSE);

        List<String> classFileForKey = generateClassFileForKey(descriptionForKey, keyCompatibleContents);

        javaProjectCopyHandler.addClassToTest(classFileForKey, containingClass);

        KeyFileCreator.createKeYFileIF(methodCorresToSE, pathToTestJava);
        KeyFileCreator.createKeYFileFunctional(methodCorresToSE, pathToTestJava);

        return pathToTestJava;
    }

    public String getMethodContractFor(
            SDGNode formalInNode,
            SDGNode formalOutNode,
            StaticCGJavaMethod methodCorresToSE) {
        String inputDescrExceptFormalIn = getInputExceptFormalIn(formalInNode, methodCorresToSE, sdg);
        String sinkDescr = generateSinkDescr(formalOutNode);
        String pointsToDecsr = PointsToGenerator.generatePreconditionFromPointsToSet(
                sdg, formalInNode, stateSaver);
        String descriptionForKey
                = "\t/*@ requires "
                + pointsToDecsr
                + ";\n\t  @ determines " + sinkDescr + " \\by "
                + inputDescrExceptFormalIn + "; */";
        return descriptionForKey;
    }

    private List<String> generateClassFileForKey(
            String descriptionForKey,
            String classContents) {

        List<String> lines = new ArrayList<>();
        for (String l : classContents.split("\n")) {
            lines.add(l);
        }

        int startLine = methodBodyListener.getStartLine();
        int stopLine = methodBodyListener.getStopLine();

        //insert nullable between passed variables
        lines.add(startLine - 1, methodBodyListener.getMethodDeclWithNullable() + " {");
        for (int i = 0; i <= stopLine - startLine; ++i) {
            lines.remove(startLine);
        }

        lines.add(startLine - 1, descriptionForKey);

        return lines;
    }

    private String generateSinkDescr(SDGNode sinkNode) {
        String bytecodeName = sinkNode.getBytecodeName();
        if (sinkNode.getKind() == SDGNode.Kind.EXIT) {
            return "\\result";
        } else {
            return "this";
        }
    }

    private String getCompleteNameOfOtherThanParam(SDGNode n, StaticCGJavaMethod methodCorresToSE) {
        String bytecodeName = n.getBytecodeName();
        String[] forInNames = bytecodeName.split("\\.");
        String inputNameForKey = forInNames[forInNames.length - 1];

        List<SDGEdge> incomingParamStructEdges = sdg.getIncomingEdgesOfKind(
                n, SDGEdge.Kind.PARAMETER_STRUCTURE);

        SDGNode currentStructSource = incomingParamStructEdges.get(0).getSource();
        for (SDGEdge e : incomingParamStructEdges) {
            if (!e.getSource().getBytecodeName().startsWith("<")
                    || e.getSource().getBytecodeName().startsWith("<param>")) {
                currentStructSource = e.getSource();
                break;
            }
        }
        String sourceBC = currentStructSource.getBytecodeName();
        while (!sourceBC.startsWith("<param>")) {
            String[] sourceBCSplit = sourceBC.split("\\.");
            inputNameForKey = sourceBCSplit[sourceBCSplit.length - 1] + "." + inputNameForKey;
            incomingParamStructEdges = sdg.getIncomingEdgesOfKind(currentStructSource, SDGEdge.Kind.PARAMETER_STRUCTURE);
            currentStructSource = incomingParamStructEdges.get(0).getSource();
            for (SDGEdge e : incomingParamStructEdges) {
                if (!e.getSource().getBytecodeName().startsWith("<") || e.getSource().getBytecodeName().startsWith("<param>")) {
                    currentStructSource = e.getSource();
                    break;
                }
            }
            sourceBC = currentStructSource.getBytecodeName();
        }
        String nameForParam = getNameForParam(sourceBC, methodCorresToSE);
        return nameForParam + "." + inputNameForKey;

    }

    private String getNameForParam(String byteCodeName, StaticCGJavaMethod methodCorresToSE) {
        int p_number = Integer.parseInt(byteCodeName.substring("<param>".length() + 1)); //+ 1 for the trailing space
        if (!methodCorresToSE.isStatic()) {
            if (p_number == 0) {
                return "this";
            } else {
                return methodBodyListener.getExtractedMethodParamNames().get(p_number - 1);
            }
        } else {
            return methodBodyListener.getExtractedMethodParamNames().get(p_number);
        }
    }

    private String getInputExceptFormalIn(
            SDGNode formalInNode,
            StaticCGJavaMethod methodCorresToSE,
            SDG sdg) {
        SDGNode methodNodeInSDG = sdg.getEntry(formalInNode);
        Set<SDGNode> formalInNodesOfProcedure = sdg.getFormalInsOfProcedure(methodNodeInSDG);
        String created = "";
        final String param = "<param>";
        for (SDGNode currentFormalInNode : formalInNodesOfProcedure) {
            String nameOfKind = currentFormalInNode.getKind().name();
            if (currentFormalInNode == formalInNode
                    || (!nameOfKind.startsWith(param) && !nameOfKind.equals("FORMAL_IN"))) {
                continue;
            }
            String bytecodeName = currentFormalInNode.getBytecodeName();
            String inputNameForKey = "";
            if (bytecodeName.startsWith(param)) {
                try {
                    inputNameForKey = getNameForParam(bytecodeName, methodCorresToSE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                inputNameForKey = getCompleteNameOfOtherThanParam(currentFormalInNode, methodCorresToSE);
            }
            if (!inputNameForKey.endsWith("<[]>")) {
                created += inputNameForKey + ", ";
            }
        }
        if (created.isEmpty()) {
            return "\\nothing";
        } else {
            return created.substring(0, created.length() - 2);
        }
    }

}
