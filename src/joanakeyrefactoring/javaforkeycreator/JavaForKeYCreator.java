/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator;

import joanakeyrefactoring.javaforkeycreator.javatokeypipeline.CopyKeYCompatibleListener;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import joanakeyrefactoring.CustomListener.AddContractsAndLoopInvariantsListener;
import joanakeyrefactoring.CustomListener.GetMethodBodyListener;
import joanakeyrefactoring.StateSaver;
import joanakeyrefactoring.ViolationsDisproverSemantic;
import joanakeyrefactoring.staticCG.JCallGraph;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaClass;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author holger
 */
public class JavaForKeYCreator {

    private final static String DOT_JAVA = ".java";
    private final static String RESULT = "\\result";
    private final static String NOTHING = "\\nothing";
    private final static String REQUIRES = "requires";
    private final static String DETERMINES = "determines";
    private final static String BY = "\\by";
    private final static String THIS = "this";
    private final static String PARAM = "<param>";
    private final static String EXCEPTION = "<exception>";
    private final static String FORMAL_IN ="FORMAL_IN";
    private final static String JOANA_ARRAY = "<[]>";
    private final static String ARRAY_TO_SEQ = "\\dl_array2seq";
    private String pathToJavaSource;
    private JCallGraph callGraph;
    private SDG sdg;
    private StateSaver stateSaver;
    private JavaProjectCopyHandler javaProjectCopyHandler;
    private CopyKeYCompatibleListener keyCompatibleListener;

    private GetMethodBodyListener methodBodyListener = new GetMethodBodyListener();

    public JavaForKeYCreator(String pathToJavaSource, JCallGraph callGraph,
                             SDG sdg, StateSaver stateSaver) {
        this.pathToJavaSource = pathToJavaSource;
        this.callGraph = callGraph;
        this.sdg = sdg;
        this.stateSaver = stateSaver;
    }

    public static String getPathToJavaClassFile(String pathToSource, StaticCGJavaClass javaClass) {
        return pathToSource + JavaProjectCopyHandler.getRelPathForJavaClass(javaClass)
                + javaClass.getOnlyClassName() + DOT_JAVA;
    }

    public String generateJavaForFormalTupleCalledFromGui(
            String contract,
            StaticCGJavaMethod method,
            String loopInvariantTemplate,
            List<String> loopInvariants,
            Map<StaticCGJavaMethod, String> methodsToMostGeneralContract) throws IOException {
        FileUtils.deleteDirectory(new File(ViolationsDisproverSemantic.PO_PATH));

        this.keyCompatibleListener = new CopyKeYCompatibleListener(callGraph.getPackageName());
        AddContractsAndLoopInvariantsListener addContractsAndLoopInvariantsListener =
                new AddContractsAndLoopInvariantsListener();
        Map<StaticCGJavaClass, Set<StaticCGJavaMethod>> allNecessaryClasses =
                callGraph.getAllNecessaryClasses(method);
        javaProjectCopyHandler =
                new JavaProjectCopyHandler(pathToJavaSource,
                                           ViolationsDisproverSemantic.PO_PATH,
                                           keyCompatibleListener);

        for (StaticCGJavaClass c : allNecessaryClasses.keySet()) {
            Set<StaticCGJavaMethod> set = allNecessaryClasses.get(c);
            String relPathForJavaClass
                    = JavaProjectCopyHandler.getRelPathForJavaClass(c);
            File javaClassFile =
                    new File(pathToJavaSource +
                             relPathForJavaClass +
                             c.getOnlyClassName() +
                             DOT_JAVA);
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
                            method, contract, loopInvariants, loopInvariantTemplate, set,
                            methodsToMostGeneralContract, keyCompatibleContents);
            javaProjectCopyHandler.copyClassContentsIntoTestDir(codeContainingContractsAndInvariants, c);
        }

        KeYFileCreator.createKeYFileIF(method, ViolationsDisproverSemantic.PO_PATH);
        KeYFileCreator.createKeYFileFunctional(method, ViolationsDisproverSemantic.PO_PATH);

        return ViolationsDisproverSemantic.PO_PATH;
    }

    public String generateJavaForFormalNodeTuple(
            SDGNodeTuple formalNodeTuple,
            StaticCGJavaMethod methodCorresToSE) throws IOException {
        this.keyCompatibleListener = new CopyKeYCompatibleListener(callGraph.getPackageName());

        SDGNode formalInNode = formalNodeTuple.getFirstNode();
        StaticCGJavaClass containingClass = methodCorresToSE.getContainingClass();
        String relPathForJavaClass
                = JavaProjectCopyHandler.getRelPathForJavaClass(containingClass);
        File javaClassFile =
                new File(pathToJavaSource +
                         relPathForJavaClass +
                         containingClass.getOnlyClassName() +
                         DOT_JAVA);

        if (!javaClassFile.exists()) { //it is a library class since it doesnt exist in the project
            throw new FileNotFoundException();
        }

        String contents = new String(Files.readAllBytes(javaClassFile.toPath()));
        Map<StaticCGJavaClass, Set<StaticCGJavaMethod>> allNecessaryClasses =
                callGraph.getAllNecessaryClasses(methodCorresToSE);

        String keyCompatibleContents = keyCompatibleListener.transformCode(
                contents, allNecessaryClasses.get(methodCorresToSE.getContainingClass()));

        methodBodyListener.parseFile(keyCompatibleContents, methodCorresToSE);

        javaProjectCopyHandler =
                new JavaProjectCopyHandler(pathToJavaSource,
                                           ViolationsDisproverSemantic.PO_PATH,
                                           keyCompatibleListener);
        javaProjectCopyHandler.copyClasses(allNecessaryClasses);

        String descriptionForKey
                = getMethodContract(
                        formalInNode, formalNodeTuple.getSecondNode(), methodCorresToSE);

        List<String> classFileForKey =
                generateClassFileForKey(descriptionForKey, keyCompatibleContents);

        javaProjectCopyHandler.addClassToTest(classFileForKey, containingClass);

        KeYFileCreator.createKeYFileIF(methodCorresToSE, ViolationsDisproverSemantic.PO_PATH);
        KeYFileCreator.createKeYFileFunctional(methodCorresToSE, ViolationsDisproverSemantic.PO_PATH);

        return ViolationsDisproverSemantic.PO_PATH;
    }

    private String getMethodContract(
            SDGNode formalInNode,
            SDGNode formalOutNode,
            StaticCGJavaMethod methodCorresToSE) throws IOException {
        this.keyCompatibleListener = new CopyKeYCompatibleListener(callGraph.getPackageName());

        StaticCGJavaClass containingClass = methodCorresToSE.getContainingClass();
        String relPathForJavaClass
                = JavaProjectCopyHandler.getRelPathForJavaClass(containingClass);
        File javaClassFile =
                new File(pathToJavaSource +
                         relPathForJavaClass +
                         containingClass.getOnlyClassName() +
                         DOT_JAVA);

        if (!javaClassFile.exists()) { //it is a library class since it doesnt exist in the project
            throw new FileNotFoundException();
        }

        String contents = new String(Files.readAllBytes(javaClassFile.toPath()));
        Map<StaticCGJavaClass, Set<StaticCGJavaMethod>> allNecessaryClasses =
                callGraph.getAllNecessaryClasses(methodCorresToSE);

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
                = "\t/*@ " + REQUIRES + " "
                + pointsToDecsr
                + ";\n\t  @ " + DETERMINES + " " + sinkDescr + " " + BY + " "
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
        this.keyCompatibleListener = new CopyKeYCompatibleListener(callGraph.getPackageName());

        StaticCGJavaClass containingClass = methodCorresToSE.getContainingClass();
        String relPathForJavaClass
                = JavaProjectCopyHandler.getRelPathForJavaClass(containingClass);
        //if (pathToJavaSource.endsWith(//"") + relPathForJavaClass)
        File javaClassFile =
                new File(pathToJavaSource +
                         relPathForJavaClass +
                         containingClass.getOnlyClassName() +
                         DOT_JAVA);

        if (!javaClassFile.exists()) { //it is a library class since it doesn't exist in the project
            throw new FileNotFoundException(javaClassFile.toString());
        }

        String contents = new String(Files.readAllBytes(javaClassFile.toPath()));
        Map<StaticCGJavaClass, Set<StaticCGJavaMethod>> allNecessaryClasses =
                callGraph.getAllNecessaryClasses(methodCorresToSE);

        String keyCompatibleContents = keyCompatibleListener.transformCode(
                contents, allNecessaryClasses.get(methodCorresToSE.getContainingClass()));

        methodBodyListener.parseFile(keyCompatibleContents, methodCorresToSE);

        //
        //most general contract, if it hasn't already been computed
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
                = "\t/*@ " + REQUIRES + " "
                + pointsToDecsr
                + ";\n\t  @ " + DETERMINES + " " + sinkDescr + " " + BY + " "
                + inputDescrExceptFormalIn + "; */";
        return descriptionForKey;
    }

    private String generateMostGeneralContract(SDGNode formalIn, SDGNode formalOut,
                                               StaticCGJavaMethod method) {
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
        return "\t/*@ " + REQUIRES + " "
                + pointsToDecsr
                + ";\n\t  @ " + DETERMINES + " " + allOutput + " " + BY + " "
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
        //String bytecodeName =
        sinkNode.getBytecodeName();
        if (sinkNode.getKind() == SDGNode.Kind.EXIT) {
            return RESULT;
        } else {
            return THIS;
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
                    || e.getSource().getBytecodeName().startsWith(PARAM)) {
                currentStructSource = e.getSource();
                break;
            }
        }
        String sourceBC = currentStructSource.getBytecodeName();
        while (!sourceBC.startsWith(PARAM) && !sourceBC.startsWith(EXCEPTION)) {
            String[] sourceBCSplit = sourceBC.split("\\.");
            inputNameForKey = sourceBCSplit[sourceBCSplit.length - 1] + "." + inputNameForKey;
            incomingParamStructEdges =
                    sdg.getIncomingEdgesOfKind(currentStructSource,
                                               SDGEdge.Kind.PARAMETER_STRUCTURE);
            if (incomingParamStructEdges == null || incomingParamStructEdges.isEmpty()) {
                // FIXME: Maybe review whether this break corrupts anything
                break;
            }
            currentStructSource = incomingParamStructEdges.get(0).getSource();
            for (SDGEdge e : incomingParamStructEdges) {
                if (!e.getSource().getBytecodeName().startsWith("<")
                        || e.getSource().getBytecodeName().startsWith(PARAM)) {
                    currentStructSource = e.getSource();
                    break;
                }
            }
            sourceBC = currentStructSource.getBytecodeName();
        }
        if (sourceBC.startsWith(PARAM)) {
            String nameForParam = getNameForParam(sourceBC, methodCorresToSE);
            return nameForParam + "." + inputNameForKey;
        } else {
            return EXCEPTION + inputNameForKey;
        }

    }

    private String getNameForParam(String byteCodeName, StaticCGJavaMethod methodCorresToSE) {
        int p_number =
                Integer.parseInt(byteCodeName.substring(PARAM.length()
                                 + 1)); //+ 1 for the trailing space
        if (!methodCorresToSE.isStatic()) {
            if (p_number == 0) {
                return THIS;
            } else {
                if (p_number <= methodBodyListener.getExtractedMethodParamNames().size()) {
                    return methodBodyListener.getExtractedMethodParamNames().get(p_number - 1);
                } else if (methodBodyListener.getMethodParamsNullable() != null) {
                    int lastSpace = methodBodyListener.getMethodParamsNullable().lastIndexOf(" ") + 1;
                    return methodBodyListener.getMethodParamsNullable().substring(lastSpace);
                } else { return ""; }
            }
        } else {
            if (p_number < methodBodyListener.getExtractedMethodParamNames().size()) {
                return methodBodyListener.getExtractedMethodParamNames().get(p_number);
            } else if (methodBodyListener.getMethodParamsNullable() != null) {
                int lastSpace = methodBodyListener.getMethodParamsNullable().lastIndexOf(" ") + 1;
                return methodBodyListener.getMethodParamsNullable().substring(lastSpace);
            } else { return ""; }
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
        for (SDGNode currentFormalInNode : formalInNodesOfProcedure) {
            String nameOfKind = currentFormalInNode.getKind().name();
            if (!(predicate.test(currentFormalInNode))
                    || (!nameOfKind.startsWith(PARAM) && !nameOfKind.equals(FORMAL_IN))) {
                continue;
            }
            String bytecodeName = currentFormalInNode.getBytecodeName();
            String inputNameForKey = "";
            if (bytecodeName.startsWith(PARAM)) {
                try {
                    inputNameForKey = getNameForParam(bytecodeName, methodCorresToSE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                inputNameForKey = getCompleteNameOfOtherThanParam(currentFormalInNode,
                                                                  methodCorresToSE);
            }
            if (inputNameForKey.endsWith(JOANA_ARRAY)) {
                inputNameForKey =
                        ARRAY_TO_SEQ + "(" + inputNameForKey.replace(JOANA_ARRAY, "") + ")";
            }
            created += inputNameForKey + ", ";
        }
        if (created.isEmpty()) {
            return NOTHING;
        } else {
            return created.substring(0, created.length() - 2);
        }
    }

}
