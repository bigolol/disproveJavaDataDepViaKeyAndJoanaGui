/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeygui.joanahandler;

import com.ibm.wala.ipa.callgraph.pruned.ApplicationLoaderPolicy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.graph.GraphIntegrity;
import edu.kit.joana.api.IFCAnalysis;
import edu.kit.joana.api.sdg.SDGConfig;
import edu.kit.joana.api.sdg.SDGFormalParameter;
import edu.kit.joana.api.sdg.SDGMethod;
import edu.kit.joana.api.sdg.SDGProgram;
import edu.kit.joana.api.sdg.SDGProgramPart;
import edu.kit.joana.ifc.sdg.mhpoptimization.MHPType;
import edu.kit.joana.ifc.sdg.util.JavaMethodSignature;
import edu.kit.joana.util.Stubs;
import edu.kit.joana.wala.core.SDGBuilder;
import java.io.IOException;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author holgerklein
 */
public class JoanaInstance {

    private String currentJarPath;

    private String currentJavaFolderPath;
    private String currentMainMethod;
    private SDGProgram program;
    private IFCAnalysis ana;
    private Collection<SDGProgramPart> allProgramParts;
    private Collection<SDGMethod> allMethods;
    private SortedSet<String> allProgramPartsString = new TreeSet<>();
    private SortedSet<String> allMethodsString = new TreeSet<>();

    public JoanaInstance(String currentJarPath, String currentJavaFolderPath, String currentMainMethod) throws ClassHierarchyException,
            IOException, GraphIntegrity.UnsoundGraphException, CancelException {
        this.currentJarPath = currentJarPath;
        this.currentJavaFolderPath = currentJavaFolderPath;
        this.currentMainMethod = currentMainMethod;
        runJoanaCreateSDGAndIFCAnalyis();
    }

    public Collection<SDGProgramPart> getAllProgramParts() {
        return allProgramParts;
    }

    public Collection<SDGMethod> getAllMethods() {
        return allMethods;
    }

    private void runJoanaCreateSDGAndIFCAnalyis() throws ClassHierarchyException,
            IOException, GraphIntegrity.UnsoundGraphException, CancelException {
        JavaMethodSignature entryMethod = JavaMethodSignature.mainMethodOfClass(currentMainMethod);
        SDGConfig config = new SDGConfig(
                currentJarPath, entryMethod.toBCString(),
                Stubs.JRE_14);
        config.setComputeInterferences(true);
        config.setMhpType(MHPType.PRECISE);
        config.setPointsToPrecision(SDGBuilder.PointsToPrecision.INSTANCE_BASED);
        config.setExceptionAnalysis(SDGBuilder.ExceptionAnalysis.INTERPROC);
        config.setFieldPropagation(SDGBuilder.FieldPropagation.OBJ_GRAPH_NO_MERGE_AT_ALL);

        // save intermediate results of SDG generation points to call graph
        config.setCGConsumer(null);
        // Schneidet beim SDG application edges raus, so besser sichtbar mit dem graphviewer
        config.setPruningPolicy(ApplicationLoaderPolicy.INSTANCE);

        program = SDGProgram.createSDGProgram(config, System.out,
                new NullProgressMonitor());
        ana = new IFCAnalysis(program);
        allProgramParts = program.getAllProgramParts();
        for (SDGProgramPart part : allProgramParts) {
            allProgramPartsString.add(part.toString());
        }
        allMethods = program.getAllMethods();
        for (SDGMethod method : allMethods) {
            for (SDGFormalParameter param : method.getParameters()) {
                allMethodsString.add(method.toString() + param.getName());
            }
        }
    }

    public Collection<String> getAllProgramPartsString() {
        return allProgramPartsString;
    }

    public Collection<String> getAllMethodsString() {
        return allMethodsString;
    }

    public SDGProgram getProgram() {
        return program;
    }

    public IFCAnalysis getAna() {
        return ana;
    }

}
