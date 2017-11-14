/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeygui.joanahandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author holgerklein
 */
public class Helper {

    private static final String DOT = ".";
    private static final String PACKAGE = "package";
    private static final String MAIN = "main";
    private static final String DOT_JAVA = ".java";

    public static List<String> getAllClassesContainingMainMethod(File parentFolder) {
        List<File> javaFiles = new ArrayList<>();
        getAllJaveFilesRec(parentFolder, javaFiles);
        List<String> created = new ArrayList<>();
        javaFiles.forEach((File f) -> {
            try {
                List<String> lines = Files.readAllLines(f.toPath());
                String currentPackage = "";
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith(PACKAGE)) {
                        currentPackage = line.substring(PACKAGE.length(), line.length() - 1).trim();
                    } else if (line.contains(MAIN)) {
                        String pkg = currentPackage + (currentPackage.equals("") ? "" : DOT);
                        String fName = f.getName();
                        created.add(pkg + fName.substring(0, fName.length() - DOT_JAVA.length()));
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return created;
    }

    public static void getAllJaveFilesRec(File currentParentFolder, List<File> javaFiles) {
        File[] subFiles = currentParentFolder.listFiles();
        for (File f : subFiles) {
            if (f.isDirectory()) {
                getAllJaveFilesRec(f, javaFiles);
            } else {
                if (f.getName().endsWith(DOT_JAVA)) {
                    javaFiles.add(f);
                }
            }
        }
    }
}
