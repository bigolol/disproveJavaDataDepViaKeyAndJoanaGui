/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeygui;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.GraphIntegrity;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import joanakeygui.joanahandler.Helper;
import joanakeygui.joanahandler.JoanaInstance;

/**
 *
 * @author holgerklein
 */
public class JoanaView {

    private File currentJarFile;
    private File currentJavaFolderFile;
    private String currentMainClass;
    private FXMLDocumentController controller;
    private JoanaInstance joanaInstance;

    public JoanaView(FXMLDocumentController controller) {
        this.controller = controller;
    }

    public void setCurrentJarFile(File jarFile) {
        if (jarFile == null) {
            return;
        }
        this.currentJarFile = jarFile;
        controller.setJarPath(jarFile.getAbsolutePath());
        //boolean correctJarFile =
        checkIfCorrectJarFile();
        //handle result
        tryCreateJoana();
    }

    public void setCurrentJavaFolderFile(File folderDir) {
        if (folderDir == null) {
            return;
        }
        this.currentJavaFolderFile = folderDir;
        controller.setFolderPath(folderDir.getAbsolutePath());
        controller.letUserChooseMainClass(
                Helper.getAllClassesContainingMainMethod(this.currentJavaFolderFile)
                );
        tryCreateJoana();
    }

    public void setCurrentMainClass(String currentMainClass) {
        this.currentMainClass = currentMainClass;
        tryCreateJoana();
    }

    private void tryCreateJoana() {
        if (currentMainClass == null || currentJavaFolderFile == null || currentJarFile == null) {
            return;
        }
        try {
            joanaInstance = new JoanaInstance(
                    currentJarFile.getAbsolutePath(),
                    currentJavaFolderFile.getAbsolutePath(),
                    currentMainClass);
            controller.letUserAddSinksAndSrcs(joanaInstance);
        } catch (ClassHierarchyException ex) {
            Logger.getLogger(JoanaView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JoanaView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GraphIntegrity.UnsoundGraphException ex) {
            Logger.getLogger(JoanaView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CancelException ex) {
            Logger.getLogger(JoanaView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean checkIfCorrectJarFile() {
        return true;
    }

    public JoanaInstance getJoanaInstance() {
        return joanaInstance;
    }

    public File getCurrentJarFile() {
        return currentJarFile;
    }

    public File getCurrentJavaFolderFile() {
        return currentJavaFolderFile;
    }

    public String getCurrentMainClass() {
        return currentMainClass;
    }
    
    

}
