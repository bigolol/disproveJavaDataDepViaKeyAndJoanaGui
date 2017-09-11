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
                    if (line.startsWith("package")) {
                        currentPackage = line.substring("package".length(), line.length() - 1).trim();
                    } else {
                        if (line.contains("main")) {
                            created.add(currentPackage + "."
                                    + f.getName().substring(0, f.getName().length() - ".java".length()));
                        }
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
                if (f.getName().endsWith(".java")) {
                    javaFiles.add(f);
                }
            }
        }
    }
}
