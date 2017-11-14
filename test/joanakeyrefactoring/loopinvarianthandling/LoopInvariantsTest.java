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

    private final static String JZIP_PATH = "testdata/jzip.dispro";

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
        String saveStr = FileUtils.readFileToString(new File(JZIP_PATH), Charset.defaultCharset());
        //DisprovingProject disprovingProject =
        DisprovingProject.generateFromSavestring(saveStr);
    }
    
}
