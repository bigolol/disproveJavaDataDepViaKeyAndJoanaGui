/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator;

import joanakeyrefactoring.javaforkeycreator.javatokeypipeline.CopyKeyCompatibleListener;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import joanakeyrefactoring.CustomListener.AddContractsAndLoopInvariantsListener;
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

    public String generateJavaForFormalTupleCalledFromGui(
            String contract,
            StaticCGJavaMethod method,
            String loopInvariantTemplate,
            List<String> loopInvariants,
            Map<StaticCGJavaMethod, String> methodsToMostGeneralContract) throws IOException {
        this.keyCompatibleListener = new CopyKeyCompatibleListener(callGraph.getPackageName());
        AddContractsAndLoopInvariantsListener addContractsAndLoopInvariantsListener = new AddContractsAndLoopInvariantsListener();
        Map<StaticCGJavaClass, Set<StaticCGJavaMethod>> allNecessaryClasses = callGraph.getAllNecessaryClasses(method);
        javaProjectCopyHandler = new JavaProjectCopyHandler(pathToJavaSource, pathToTestJava, keyCompatibleListener);
        for (StaticCGJavaClass c : allNecessaryClasses.keySet()) {
            Set<StaticCGJavaMethod> set = allNecessaryClasses.get(c);
            String relPathForJavaClass
                    = JavaProjectCopyHandler.getRelPathForJavaClass(c);
            File javaClassFile = new File(pathToJavaSource + relPathForJavaClass + c.getOnlyClassName() + ".java");
            if (!javaClassFile.exists()) {
                //it is a library class since it doesnt exist in the project
                //but since the violationswrapper sorts out all methods which arent key compatible
                //it should be a key compatible library method 
                continue;
            }
            String rawFileContents = new String(Files.readAllBytes(javaClassFile.toPath()));
            String keyCompatibleContents = keyCompatibleListener.transformCode(
                    rawFileContents, allNecessaryClasses.get(c));
            String codeContainingContractsAndInvariants
                    = addContractsAndLoopInvariantsListener.addContractsAndLoopInvariants(
                            method, contract, loopInvariants, loopInvariantTemplate, set, methodsToMostGeneralContract, keyCompatibleContents);
            javaProjectCopyHandler.copyClassContentsIntoTestDir(codeContainingContractsAndInvariants, c);
        }

        KeyFileCreator.createKeYFileIF(method, pathToTestJava);
        KeyFileCreator.createKeYFileFunctional(method, pathToTestJava);

        return pathToTestJava;
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

        String descriptionForKey
                = getMethodContract(
                        formalInNode, formalNodeTuple.getSecondNode(), methodCorresToSE);

        List<String> classFileForKey = generateClassFileForKey(descriptionForKey, keyCompatibleContents);

        javaProjectCopyHandler.addClassToTest(classFileForKey, containingClass);

        KeyFileCreator.createKeYFileIF(methodCorresToSE, pathToTestJava);
        KeyFileCreator.createKeYFileFunctional(methodCorresToSE, pathToTestJava);

        return pathToTestJava;
    }

    private String getMethodContract(
            SDGNode formalInNode,
            SDGNode formalOutNode,
            StaticCGJavaMethod methodCorresToSE) throws IOException {
        this.keyCompatibleListener = new CopyKeyCompatibleListener(callGraph.getPackageName());

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

        String inputDescrExceptFormalIn = getAllInputIf((n) -> {
            return n != formalInNode;
        }, formalInNode, methodCorresToSE, sdg);
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

    public String getMethodContractAndSetLoopInvariantAndSetMostGeneralContract(
            SDGNode formalInNode,
            SDGNode formalOutNode,
            StaticCGJavaMethod methodCorresToSE,
            Map<SDGEdge, String> edgeToInvariantTempltate,
            SDGEdge e,
            Map<StaticCGJavaMethod, String> methodToGeneralContract) throws IOException {
        this.keyCompatibleListener = new CopyKeyCompatibleListener(callGraph.getPackageName());

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

        //
        //most general contract, if it hasnt already been computed
        //
        String generalContract = methodToGeneralContract.get(methodCorresToSE);
        if (generalContract == null) {
            String mostGeneralContract = generateMostGeneralContract(
                    formalInNode, formalOutNode, methodCorresToSE);
            methodToGeneralContract.put(
                    methodCorresToSE, mostGeneralContract);
        }

        String inputDescrExceptFormalIn = getAllInputIf((n) -> {
            return n != formalInNode;
        }, formalInNode, methodCorresToSE, sdg);
        String sinkDescr = generateSinkDescr(formalOutNode);
        String pointsToDecsr = PointsToGenerator.generatePreconditionFromPointsToSet(
                sdg, formalInNode, stateSaver);

        edgeToInvariantTempltate.put(e,
                LoopInvariantGenerator.createLoopInvariant(
                        sinkDescr, inputDescrExceptFormalIn));

        String descriptionForKey
                = "\t/*@ requires "
                + pointsToDecsr
                + ";\n\t  @ determines " + sinkDescr + " \\by "
                + inputDescrExceptFormalIn + "; */";
        return descriptionForKey;
    }

    private String generateMostGeneralContract(SDGNode formalIn, SDGNode formalOut, StaticCGJavaMethod method) {
        String allInput = getAllInputIf((n) -> {
            return true;
        }, formalIn, method, sdg);
        SDGNode methodNode = sdg.getEntry(formalIn);
        Set<SDGNode> formalOuts = sdg.getFormalInsOfProcedure(methodNode);
        HashSet<String> allOutStrings = new HashSet<>();
        String allOutput = "";
        for (SDGNode n : formalOuts) {
            String sinkDescr = generateSinkDescr(n);
            if (!allOutStrings.contains(sinkDescr)) {
                allOutStrings.add(sinkDescr);
                allOutput += sinkDescr + ", ";
            }
        }
        if (allOutput.lastIndexOf(",") != -1) {
            allOutput = allOutput.substring(0, allOutput.length() - 2);
        }
        String pointsToDecsr = PointsToGenerator.generatePreconditionFromPointsToSet(
                sdg, formalIn, stateSaver);
        return "\t/*@ requires "
                + pointsToDecsr
                + ";\n\t  @ determines " + allOutput + " \\by "
                + allInput + "; */";
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
        while (!sourceBC.startsWith("<param>") && !sourceBC.startsWith("<exception>")) {
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
        if (sourceBC.startsWith("<param>")) {
            String nameForParam = getNameForParam(sourceBC, methodCorresToSE);
            return nameForParam + "." + inputNameForKey;
        } else {
            return "<exception>" + inputNameForKey;
        }

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

    private String getAllInputIf(
            Predicate<SDGNode> predicate,
            SDGNode formalInNode,
            StaticCGJavaMethod methodCorresToSE,
            SDG sdg) {
        SDGNode methodNodeInSDG = sdg.getEntry(formalInNode);
        Set<SDGNode> formalInNodesOfProcedure = sdg.getFormalInsOfProcedure(methodNodeInSDG);
        String created = "";
        final String param = "<param>";
        for (SDGNode currentFormalInNode : formalInNodesOfProcedure) {
            String nameOfKind = currentFormalInNode.getKind().name();
            if (!(predicate.test(currentFormalInNode))
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
