/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.loopinvarianthandling;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import joanakeyrefactoring.persistence.DisprovingProject;
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
public class LoopInvariantsTest {
    
    public LoopInvariantsTest() {
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
    public void testCalcAllRelLoopPos() throws IOException {
        String pathToDistro = "testdata/jzip.dispro";
        String saveStr = FileUtils.readFileToString(new File(pathToDistro), Charset.defaultCharset());
        //DisprovingProject disprovingProject =
        DisprovingProject.generateFromSavestring(saveStr);
    }
    
}
