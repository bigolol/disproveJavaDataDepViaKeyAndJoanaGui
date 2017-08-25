/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.persistence;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.GraphIntegrity;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import joanakeyrefactoring.CombinedApproach;
import joanakeyrefactoring.JoanaAndKeyCheckData;
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

    @Test
    public void testSaveAndLoad() throws 
            IOException, ClassHierarchyException, 
            GraphIntegrity.UnsoundGraphException,
            CancelException {
        String pathToDistro = "/home/holger/Code/hiwi/DisproveViaKeyAndJoanaGUI/testdata/multipleClassesArrFalsePos.dispro";
        String fileContents = FileUtils.readFileToString(new File(pathToDistro), Charset.defaultCharset());
        DisprovingProject disprovingProject = DisprovingProject.generateFromSavestring(fileContents);
    }

}
