package joanakeyrefactoring;

import com.ibm.wala.ipa.callgraph.pruned.ApplicationLoaderPolicy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;
import edu.kit.joana.api.IFCAnalysis;
import edu.kit.joana.api.annotations.IFCAnnotation;
import edu.kit.joana.api.lattice.BuiltinLattices;
import edu.kit.joana.api.sdg.SDGCall;
import edu.kit.joana.api.sdg.SDGConfig;
import edu.kit.joana.api.sdg.SDGProgram;
import edu.kit.joana.api.sdg.SDGProgramPart;
import edu.kit.joana.ifc.sdg.core.SecurityNode;
import edu.kit.joana.ifc.sdg.core.violations.IViolation;
import edu.kit.joana.ifc.sdg.mhpoptimization.MHPType;
import edu.kit.joana.ifc.sdg.util.JavaMethodSignature;
import edu.kit.joana.util.Stubs;
import edu.kit.joana.wala.core.SDGBuilder.ExceptionAnalysis;
import edu.kit.joana.wala.core.SDGBuilder.FieldPropagation;
import edu.kit.joana.wala.core.SDGBuilder.PointsToPrecision;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class CombinedApproach {

//    public static void main(String[] args) {
//        try {
//            JoanaAndKeyCheckData parsedCheckData = CombinedApproach.parseInputFile("testdata/jzip.joak");
//            CombinedApproach.runTestFromCheckData(parsedCheckData);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void runTestFromCheckData(JoanaAndKeYCheckData checkData)
            throws ClassHierarchyException, IOException, UnsoundGraphException,
            CancelException, CouldntAddAnnoException, CancelException {
        checkData.addAnnotations();
        AutomationHelper automationHelper = new AutomationHelper(checkData.getPathToJavaFile());
        ViolationsDisproverSemantic violationsViaKeyChecker
                = new ViolationsDisproverSemantic(
                        automationHelper, checkData);
        checkAnnotatedPDGWithJoanaAndKey(checkData.getAnalysis(), violationsViaKeyChecker);
    }

    public static void checkAnnotatedPDGWithJoanaAndKey(
            IFCAnalysis annotatedAnalysis, ViolationsDisproverSemantic violationChecker)
                    throws FileNotFoundException {
        Collection<? extends IViolation<SecurityNode>> violations = annotatedAnalysis.doIFC();
        try {
            violationChecker.disproveViaKey(annotatedAnalysis, violations,
                                            annotatedAnalysis.getProgram().getSDG());
        } catch (IOException ex) {
            Logger.getLogger(CombinedApproach.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void addAnnotationsFileBased(
            IFCAnalysis ana,
            List<String> annotationsSink,
            List<String> annotationsSource,
            String annotationPath) throws IOException {
        FileInputStream annotationSourceStream = new FileInputStream(annotationPath);
        Collection<IFCAnnotation> annotationCollection
                = ExtractAnnotations.loadAnnotations(annotationSourceStream, ana.getProgram().getSDG());
        annotationCollection.forEach((annotation) -> {
            ana.addAnnotation(annotation);
        });
    }

    public static JoanaAndKeYCheckData parseInputFile(File filetoload)
            throws IOException, ClassHierarchyException, UnsoundGraphException, CancelException {
        BufferedReader br = new BufferedReader(new FileReader(filetoload));
        StringBuilder completeString = new StringBuilder();
        completeString.append("{\n");
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (line.trim().startsWith("//")) {
                continue;
            }
            completeString.append(line + '\n');
        }
        br.close();
        completeString.append("}\n");

        JSONObject jsonObj = new JSONObject(completeString.toString());

        String pathKeY = jsonObj.getString("pathKeY");
        String pathToJar = jsonObj.getString("pathToJar");
        String pathToJavaFile = jsonObj.getString("pathToJavaFile");
        String entryMethodString = jsonObj.getString("entryMethod");
        JavaMethodSignature entryMethod = JavaMethodSignature.mainMethodOfClass(entryMethodString);
        String annotationPath = jsonObj.getString("annotationPath");
        boolean fullyAutomatic = jsonObj.getBoolean("fullyAutomatic");

        StateSaver stateSaver = new StateSaver();

        IFCAnalysis analysis = runJoanaCreateSDGAndIFCAnalyis(pathToJar, entryMethod, stateSaver);
        
        stateSaver.generatePersistenseStructures(analysis.getProgram().getSDG());

        JSONArray sources = jsonObj.getJSONArray("sources");
        JSONArray sinks = jsonObj.getJSONArray("sinks");

        ArrayList<SingleAnnotationAdder> singleAnnotationAdders = new ArrayList<>();

        sources.forEach((src) -> {
            singleAnnotationAdders.add(
                    createAnnotationAdder((JSONObject) src, (part, sec) -> {
                        analysis.addSourceAnnotation(part, sec);
                    }, analysis)
            );
        });

        sinks.forEach((sink) -> {
            singleAnnotationAdders.add(
                    createAnnotationAdder((JSONObject) sink, (part, sec) -> {
                        analysis.addSinkAnnotation(part, sec);
                    }, analysis)
            );
        });

        return new JoanaAndKeYCheckData(
                pathKeY, pathToJar, pathToJavaFile, entryMethodString, annotationPath,
                entryMethod, fullyAutomatic, analysis, singleAnnotationAdders, stateSaver);
    }

    public static SingleAnnotationAdder
            createAnnotationAdder(JSONObject jsonObj,
                                  BiConsumer<SDGProgramPart, String> annoAddMethod,
                                  IFCAnalysis analysis) {
        String securityLevelString = jsonObj.getString("securityLevel");
        String securityLevelLattice = "";
        if (securityLevelString.equals("high")) {
            securityLevelLattice = BuiltinLattices.STD_SECLEVEL_HIGH;
        } else if (securityLevelString.equals("low")) {
            securityLevelLattice = BuiltinLattices.STD_SECLEVEL_LOW;
        }

        Collection<SDGProgramPart> allProgramParts = analysis.getProgram().getAllProgramParts();

        JSONObject description = jsonObj.getJSONObject("description");
        String from = description.getString("from");
        Supplier<Collection<SDGProgramPart>> partSupplier = null;
        if (from.equals("callsToMethod")) {
            int paramPos = description.getInt("paramPos");
            String method = description.getString("method");
            partSupplier
                    = () -> {
                        Collection<SDGCall> allCallsToMethod =
                                analysis.getProgram()
                                .getCallsToMethod(JavaMethodSignature.fromString(method));
                        List<SDGProgramPart> collectedParts =
                                allCallsToMethod.stream().map((SDGCall call) -> {
                            return (SDGProgramPart) call.getActualParameter(paramPos);
                        }).collect(Collectors.toList());

                        return collectedParts;
                    };
        } else if (from.equals("programPart")) {
            String programPartString = description.getString("programPart");
            partSupplier
                    = () -> {
                        Collection<SDGProgramPart> created = new ArrayList<>();
                        for (SDGProgramPart p : allProgramParts) {
                            if (p.toString().equals(programPartString)) {
                                created.add(p);
                            }
                        }
                        return created;
                    };
        }

        return new SingleAnnotationAdder(partSupplier, annoAddMethod, securityLevelLattice);

    }

    public static IFCAnalysis runJoanaCreateSDGAndIFCAnalyis(String pathToJar,
            JavaMethodSignature entryMethod, StateSaver stateSaver) throws ClassHierarchyException,
            IOException, UnsoundGraphException, CancelException {
        SDGConfig config = new SDGConfig(
                pathToJar, entryMethod.toBCString(),
                Stubs.JRE_14);
        config.setComputeInterferences(true);
        config.setMhpType(MHPType.PRECISE);
        config.setPointsToPrecision(PointsToPrecision.INSTANCE_BASED);
        config.setExceptionAnalysis(ExceptionAnalysis.INTERPROC);
        config.setFieldPropagation(FieldPropagation.OBJ_GRAPH_NO_MERGE_AT_ALL);

        // save intermediate results of SDG generation points to call graph
        config.setCGConsumer(stateSaver);
        // Schneidet beim SDG application edges raus, so besser sichtbar mit dem graphviewer
        config.setPruningPolicy(ApplicationLoaderPolicy.INSTANCE);

        SDGProgram program = SDGProgram.createSDGProgram(config, System.out,
                new NullProgressMonitor());
        IFCAnalysis ana = new IFCAnalysis(program);
        return ana;
    }
}
