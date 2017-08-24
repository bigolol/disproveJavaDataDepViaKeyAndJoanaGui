/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring;

import edu.kit.joana.api.IFCAnalysis;
import edu.kit.joana.ifc.sdg.util.JavaMethodSignature;
import java.util.List;

/**
 *
 * @author holger
 */
public class JoanaAndKeyCheckData {

    private final String pathKeY;
    private final String pathToJar;
    private final String pathToJavaFile;
    private final String entryMethodString;
    private final String annotationPath;
    private final JavaMethodSignature entryMethod;
    private final boolean fullyAutomatic;
    private final IFCAnalysis analysis;
    private final List<SingleAnnotationAdder> singleAnnotationAdders;
    private final StateSaver stateSaver;

    public JoanaAndKeyCheckData(String pathKeY, String pathToJar, String pathToJavaFile, String entryMethodString, String annotationPath, JavaMethodSignature entryMethod, boolean fullyAutomatic, IFCAnalysis analysis, List<SingleAnnotationAdder> singleAnnotationAdders, StateSaver stateSaver) {
        this.pathKeY = pathKeY;
        this.pathToJar = pathToJar;
        this.pathToJavaFile = pathToJavaFile;
        this.entryMethodString = entryMethodString;
        this.annotationPath = annotationPath;
        this.entryMethod = entryMethod;
        this.fullyAutomatic = fullyAutomatic;
        this.analysis = analysis;
        this.singleAnnotationAdders = singleAnnotationAdders;
        this.stateSaver = stateSaver;
    }

    public StateSaver getStateSaver() {
        return stateSaver;
    }

    public boolean isFullyAutomatic() {
        return fullyAutomatic;
    }

    public JavaMethodSignature getEntryMethod() {
        return entryMethod;
    }

    public String getPathKeY() {
        return pathKeY;
    }

    public String getPathToJar() {
        return pathToJar;
    }

    public String getPathToJavaFile() {
        return pathToJavaFile;
    }

    public String getEntryMethodString() {
        return entryMethodString;
    }

    public String getAnnotationPath() {
        return annotationPath;
    }

    public IFCAnalysis getAnalysis() {
        return analysis;
    }

    public void addAnnotations() {
        singleAnnotationAdders.forEach((adder) -> {
            adder.addYourselfToAnalysis();
        });
    }
}
