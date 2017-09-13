package joanakeyrefactoring;

import edu.kit.joana.api.IFCAnalysis;
import java.io.IOException;
import java.util.Collection;

import edu.kit.joana.ifc.sdg.core.SecurityNode;
import edu.kit.joana.ifc.sdg.core.violations.IViolation;
import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import edu.kit.joana.ifc.sdg.graph.chopper.RepsRosayChopper;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import joanakeyrefactoring.javaforkeycreator.JavaForKeyCreator;
import joanakeyrefactoring.staticCG.JCallGraph;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

public class ViolationsDisproverSemantic {

    public String pathToJar;
    public RepsRosayChopper chopper;
    public StateSaver stateSaver;
    //private String pathToJava;
    //private AutomationHelper automationHelper;
    //private boolean fullyAutomatic;
    private String pathToKeyJar;
    private ViolationsWrapper violationsWrapper;
    private JavaForKeyCreator javaForKeyCreator;
    private JCallGraph callGraph = new JCallGraph();

    public ViolationsDisproverSemantic(
            AutomationHelper automationHelper,
            JoanaAndKeyCheckData checkData) throws IOException {
        //this.automationHelper = automationHelper;
        this.pathToJar = checkData.getPathToJar();
        this.stateSaver = checkData.getStateSaver();
        //this.fullyAutomatic = checkData.isFullyAutomatic();
        this.pathToKeyJar = checkData.getPathKeY();
        //this.pathToJava = checkData.getPathToJavaFile();
        javaForKeyCreator = new JavaForKeyCreator(
                checkData.getPathToJavaFile(),
                callGraph,
                checkData.getAnalysis().getProgram().getSDG(),
                stateSaver);

        callGraph.generateCG(new File(pathToJar));
    }

    public void disproveViaKey(IFCAnalysis analysis, Collection<? extends IViolation<SecurityNode>> violations,
            SDG sdg) throws IOException {
        violationsWrapper = new ViolationsWrapper(
                violations, sdg, analysis, callGraph);

        while (!violationsWrapper.allCheckedOrDisproved()) {
            SDGEdge nextSummaryEdge = violationsWrapper.nextSummaryEdge();
            if (canDisproveSummaryEdge(nextSummaryEdge, sdg)) {
                violationsWrapper.removeEdge(nextSummaryEdge);
            } else {
                violationsWrapper.checkedEdge(nextSummaryEdge);
            }
        }
    }

    private boolean canDisproveSummaryEdge(SDGEdge se, SDG sdg) throws IOException {
        SDGNode actualInNode = se.getSource();
        SDGNode actualOutNode = se.getTarget();

        Collection<SDGNodeTuple> formalNodePairs = sdg.getAllFormalPairs(actualInNode, actualOutNode);

        for (SDGNodeTuple formalNodeTuple : formalNodePairs) {
            boolean result = false, resultFunc = false;
            try {
                StaticCGJavaMethod methodCorresToSummaryEdge =
                        violationsWrapper.getMethodCorresToSummaryEdge(se);
                //String pathToTestJava =
                javaForKeyCreator.generateJavaForFormalNodeTuple(formalNodeTuple,
                                                                 methodCorresToSummaryEdge);
                result = AutomationHelper.runKeY(pathToKeyJar, "proofObs/proofs" ,"information flow");
                resultFunc = AutomationHelper.runKeY(pathToKeyJar, "proofObs/proofs", "functional");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ViolationsDisproverSemantic.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (IOException ex) {
                Logger.getLogger(ViolationsDisproverSemantic.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (Exception ex) {
                Logger.getLogger(ViolationsDisproverSemantic.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            if (!result || !resultFunc) {
                return false;
            }
        }
        return true;
    }

    /*private void checkViaUser(SDGNode formalInNode, SDGNode formalOutNode, String methodNameKey) {
        if (!fullyAutomatic) {
            System.out.println("From node: " + formalInNode + " to node: " + formalOutNode);
            System.out.println("Type \"y\" to verify method manually or \"n\" to go on automatically ");
            Scanner scanInput = new Scanner(System.in);
            String keyAnswer = scanInput.nextLine();
            if (keyAnswer.equals("y")) {
                // open JAVA and KeY
                automationHelper.openKeY(pathToJar, methodNameKey);
                System.out.println("Type y if KeY could prove");
                Scanner scanInput2 = new Scanner(System.in);
                String keyAnswer2 = scanInput2.nextLine();
            }
        }
    }*/

}
