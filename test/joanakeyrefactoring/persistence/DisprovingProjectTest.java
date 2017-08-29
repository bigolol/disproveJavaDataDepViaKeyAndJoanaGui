/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.persistence;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.GraphIntegrity;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNodeTuple;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import joanakeyrefactoring.CombinedApproach;
import joanakeyrefactoring.SummaryEdgeAndMethodToCorresData;
import joanakeyrefactoring.javaforkeycreator.JavaForKeyCreator;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author holger
 */
public class DisprovingProjectTest {

    public DisprovingProjectTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    public static void main(String[] args) throws
            IOException, ClassHierarchyException,
            GraphIntegrity.UnsoundGraphException,
            CancelException {
//        DisprovingProject generateFromCheckdata = DisprovingProject.generateFromCheckdata(CombinedApproach.parseInputFile(new File("testdata/jzip.joak")));
//        generateFromCheckdata.saveSDG();
//        String generateSaveString = generateFromCheckdata.generateSaveString();
//        FileWriter writer = new FileWriter(new File("testdata/jzip.dispro"));
//        writer.write(generateSaveString);
//        writer.close();
        //DisprovingProject generateFromSavestring = DisprovingProject.generateFromSavestring(generateSaveString);
    }

    @Test
    public void testLoading() throws
            IOException, ClassHierarchyException,
            GraphIntegrity.UnsoundGraphException,
            CancelException {
//        DisprovingProject generateFromCheckdata =
//                DisprovingProject.generateFromCheckdata(
//                        CombinedApproach.parseInputFile(new File("testdata/jzip.joak")));
//        
//        generateFromCheckdata.saveSDG();
//        String generateSaveString = generateFromCheckdata.generateSaveString();
//        FileWriter fileWriter = new FileWriter(new File("testdata/jzip.dispro"));
//        fileWriter.write(generateSaveString);
//        fileWriter.close();

        DisprovingProject generateFromSavestring = DisprovingProject.generateFromSavestring(
                FileUtils.readFileToString(
                        new File("testdata/jzip.dispro"),
                        Charset.defaultCharset()));
        JavaForKeyCreator creator = new JavaForKeyCreator(generateFromSavestring.getPathToJava(), generateFromSavestring.getCallGraph(),
                generateFromSavestring.getSdg(), generateFromSavestring.getStateSaver());
        SDGEdge e = generateFromSavestring.getViolationsWrapper().getSummaryEdgesAndCorresJavaMethods().keySet().iterator().next();
        StaticCGJavaMethod corresMethod = generateFromSavestring.getViolationsWrapper().getMethodCorresToSummaryEdge(e);
        SDGNodeTuple tuple = generateFromSavestring.getSdg().getAllFormalPairs(e.getSource(), e.getTarget()).iterator().next();
        SummaryEdgeAndMethodToCorresData summaryEdgeToCorresData = generateFromSavestring.getSummaryEdgeToCorresData();

        creator.
                generateJavaForFormalTupleCalledFromGui(
                        summaryEdgeToCorresData.getContractFor(tuple),
                        corresMethod,
                        summaryEdgeToCorresData.getEdgeToLoopInvariantTemplate().get(e),
                        summaryEdgeToCorresData.getEdgeToLoopInvariant().get(e),
                        summaryEdgeToCorresData.getMethodToMostGeneralContract()
                );
    }

    @Test
    public void testStateSaverSaveIds() throws
            IOException, ClassHierarchyException,
            GraphIntegrity.UnsoundGraphException,
            CancelException {
    }

    @Test
    public void testSaveAndLoad() throws
            IOException, ClassHierarchyException,
            GraphIntegrity.UnsoundGraphException,
            CancelException {
//        DisprovingProject disprovingProject
//                = DisprovingProject.generateFromCheckdata(CombinedApproach.parseInputFile(new File("testdata/multipleClassesArrFalsePos.joak")));
//        disprovingProject.saveSDG();
//        String saveStr = disprovingProject.generateSaveString();
//        DisprovingProject loadedDisprovingProject = DisprovingProject.generateFromSavestring(saveStr);
//        SDG sdg = disprovingProject.getSdg();
//        SDG loadedsdg = loadedDisprovingProject.getSdg();
//        SDGEdge nextSummaryEdge = loadedDisprovingProject.getViolationsWrapper().nextSummaryEdge();
//        Collection<SDGNodeTuple> allFormalPairs = sdg.getAllFormalPairs(nextSummaryEdge.getSource(), nextSummaryEdge.getTarget());
//        SDGNodeTuple nodeTuple = allFormalPairs.iterator().next();
//        SDGNode entry = sdg.getEntry(nodeTuple.getFirstNode());
//        int cgNodeId = sdg.getCGNodeId(entry);
//        SDGNode loadedentry = loadedsdg.getEntry(nodeTuple.getFirstNode());
//        int loadedcgNodeId = loadedsdg.getCGNodeId(loadedentry);
//        
//        SDG manualyLoadedSDG = SDG.readFrom(new StringReader(SDGSerializer.toPDGFormat(sdg)));
//        int manualCGNodeId = manualyLoadedSDG.getCGNodeId(manualyLoadedSDG.getEntry(nodeTuple.getFirstNode()));
//        
//        //sdg serializer DOES NOT SAVE CG IDS ILL FLIP MY FUCKING SHIT AHHHHHHH
//        
//        Assert.assertNotEquals(-1, cgNodeId);
//        Assert.assertNotEquals(-1, loadedcgNodeId);
    }

}
