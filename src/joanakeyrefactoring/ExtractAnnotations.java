package joanakeyrefactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import edu.kit.joana.api.annotations.AnnotationType;
import edu.kit.joana.api.annotations.IFCAnnotation;
import edu.kit.joana.api.lattice.BuiltinLattices;
import edu.kit.joana.api.sdg.SDGCall;
import edu.kit.joana.api.sdg.SDGCallPart;
import edu.kit.joana.api.sdg.SDGProgram;
import edu.kit.joana.ifc.sdg.graph.SDG;

public class ExtractAnnotations {

    public static Collection<IFCAnnotation> loadAnnotations(InputStream source, SDG sdg) throws IOException {
        Set<IFCAnnotation> toAnnotate = new LinkedHashSet<IFCAnnotation>();
        SDGProgram program = new SDGProgram(sdg);
        BufferedReader in = new BufferedReader(new InputStreamReader(source));
        String nextLine = in.readLine();
        while (nextLine != null) {
            StringTokenizer tok = new StringTokenizer(nextLine, ",");
            AnnotationType annType = AnnotationType.valueOf(tok.nextToken());
            String level = annType == AnnotationType.SOURCE ? BuiltinLattices.STD_SECLEVEL_HIGH : BuiltinLattices.STD_SECLEVEL_LOW;
            String methodSig = tok.nextToken();
            int bcIndex = Integer.parseInt(tok.nextToken());
            String param = tok.nextToken();
            SDGCallPart callPart = null;
            int paramNum = -3;
            for (SDGCall call : program.getMethod(methodSig).getAllCalls()) {
                if (call.getBytecodeIndex() != bcIndex) {
                    continue;
                }
                if ("ret".equals(param)) {
                    callPart = call.getReturn();
                    paramNum = -1;
                } else if ("exc".equals(param)) {
                    callPart = call.getExceptionNode();
                    paramNum = -2;
                } else {
                    paramNum = Integer.parseInt(param);
                    if (paramNum == 0) {
                        callPart = call.getThis();
                    } else {
                        callPart = call.getActualParameter(paramNum);
                    }
                }
            }
            toAnnotate.add(new IFCAnnotation(annType, level, callPart));
            nextLine = in.readLine();
        }
        return toAnnotate;
    }
}
