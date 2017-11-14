/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator;

import joanakeyrefactoring.javaforkeycreator.javatokeypipeline.CopyKeYCompatibleListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaClass;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author holger
 */
public class JavaProjectCopyHandler {

    private final static String DOT_JAVA = ".java";
    private String pathToSource;
    private String pathToNew;
    private File newFile;
    private CopyKeYCompatibleListener copyKeyCompatibleListener;
    private Map<StaticCGJavaClass, ArrayList<LoopInvariant>> loopInvariants = new HashMap<>();
    
    public JavaProjectCopyHandler(String pathToSource, String pathToNew,
                                  CopyKeYCompatibleListener copyKeyCompatibleListener)
                   throws IOException {
        this.pathToSource = pathToSource;
        this.pathToNew = pathToNew;
        this.copyKeyCompatibleListener = copyKeyCompatibleListener;
        newFile = new File(pathToNew);
        clearFolder();
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
    }
    
    public void clearFolder() throws IOException {
        FileUtils.deleteDirectory(newFile);
    }
    
    public static String getRelPathForJavaClass(StaticCGJavaClass cgjs) {
        String packageString = cgjs.getPackageString();
        String packageToRelPathString = "";
        if (packageString != null) {
            packageToRelPathString = "/" + packageString.replaceAll("\\.", "/") + "/";
        }
        return packageToRelPathString;
    }
    
    public void copyClassContentsIntoTestDir(String contents,
                                             StaticCGJavaClass javaClass)
                                     throws FileNotFoundException, IOException {
        String relPathForJavaClass = getRelPathForJavaClass(javaClass);
        String className = javaClass.getOnlyClassName();
        File relPathFile = new File(pathToNew + "/" + relPathForJavaClass);
        relPathFile.mkdirs();
        File javaFile = new File(pathToNew + relPathForJavaClass + "/" + className + DOT_JAVA);
        javaFile.createNewFile();
        PrintWriter out = new PrintWriter(javaFile);
        out.print(contents);
        out.close();
    }
    
    public void addClassToTest(List<String> classContent, StaticCGJavaClass javaClass)
            throws FileNotFoundException, IOException {
        StringBuilder builder = new StringBuilder();
        classContent.forEach((s) -> {
            builder.append(s).append('\n');
        });
        copyClassContentsIntoTestDir(builder.toString(), javaClass);
    }
    
    public void copyClasses(Map<StaticCGJavaClass, Set<StaticCGJavaMethod>> classesToCopy)
            throws IOException {
        for (StaticCGJavaClass currentClassToCopy : classesToCopy.keySet()) {
            String relPathForJavaClass = getRelPathForJavaClass(currentClassToCopy);
            String className = currentClassToCopy.getOnlyClassName();
            
            File folderPathNewNew = new File(pathToNew + relPathForJavaClass);
            if (!folderPathNewNew.exists()) {
                folderPathNewNew.mkdirs();
            }
            try {
                File classFileToCopyTo =
                        new File(pathToNew + relPathForJavaClass + className + DOT_JAVA);
                File classFileToCopyFrom =
                        new File(pathToSource + relPathForJavaClass + className + DOT_JAVA);
                
                String contents =
                        new String(java.nio.file.Files.readAllBytes(classFileToCopyFrom.toPath()));
                
                String keyCompatibleContents =
                        copyKeyCompatibleListener.transformCode(contents,
                                                                classesToCopy.get(currentClassToCopy));
                
                String keyCompWithLoopInvariants =
                        addLoopInvariantsIfNeeded(keyCompatibleContents, currentClassToCopy);
                Charset encoding = null;
                FileUtils.writeStringToFile(classFileToCopyTo, keyCompWithLoopInvariants, encoding);
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
        }
    }
    
    private String addLoopInvariantsIfNeeded(String code, StaticCGJavaClass c) {
        if (!loopInvariants.containsKey(c)) {
            createLoopInvarsFor(code, c);
        }
        int amtCharsAdded = 0;
        for (LoopInvariant loopInvariant : loopInvariants.get(c)) {
            code = code.substring(0, loopInvariant.getStartChar() + amtCharsAdded)
                    + loopInvariant.getInvariant()
                    + code.substring(loopInvariant.getStartChar() + amtCharsAdded, code.length());
            amtCharsAdded += loopInvariant.getInvariant().length();
        }
        return code;
    }
    
    private void createLoopInvarsFor(String code, StaticCGJavaClass c) {
        LoopListener loopListener = new LoopListener();
        List<Integer> loopLines = loopListener.findLoopLines(code);
        ArrayList<LoopInvariant> created = new ArrayList<>();
        for (int i = 0; i < loopLines.size(); i += 2) {
            int currLoopStart = loopLines.get(i);
            int currLoopEnd = loopLines.get(i + 1);
            String currLoopStr = code.substring(currLoopStart, currLoopEnd);
            created.add(new LoopInvariant(currLoopStr, currLoopStart, currLoopEnd));
        }
        loopInvariants.put(c, created);
    }
    
}
