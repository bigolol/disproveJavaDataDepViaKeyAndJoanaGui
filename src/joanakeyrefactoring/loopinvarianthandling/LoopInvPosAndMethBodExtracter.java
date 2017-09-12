/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.loopinvarianthandling;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import joanakeyrefactoring.javaforkeycreator.JavaForKeyCreator;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaClass;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author holger
 */
public class LoopInvPosAndMethBodExtracter {

    public void findAllLoopPositionsAndMethodBodies(Collection<StaticCGJavaMethod> methods,
                                                    String pathToSource)
                                                            throws IOException {
        AddLoopPosAndMethodBodiesListener listener = new AddLoopPosAndMethodBodiesListener();
        HashSet<StaticCGJavaClass> neededClasses = new HashSet<>();

        for (StaticCGJavaMethod m : methods) {
            if (!neededClasses.contains(m.getContainingClass())) {
                neededClasses.add(m.getContainingClass());
            }
        }

        for (StaticCGJavaClass c : neededClasses) {
            try {
                String pathToClassesJavaFile =
                        JavaForKeyCreator.getPathToJavaClassFile(pathToSource, c);
                String javaClassContents =
                        FileUtils.readFileToString(new File(pathToClassesJavaFile),
                                                   Charset.defaultCharset());
                listener.findAllLoopsAndAddToMethods(javaClassContents, methods, c);
            } catch (Exception e) {
            }
        }
    }

}
