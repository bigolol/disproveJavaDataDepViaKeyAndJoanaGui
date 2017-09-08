/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.CustomListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import joanakeyrefactoring.antlr.java8.Java8BaseListener;
import joanakeyrefactoring.antlr.java8.Java8Lexer;
import joanakeyrefactoring.antlr.java8.Java8Parser;
import joanakeyrefactoring.javaforkeycreator.javatokeypipeline.CopyKeyCompatibleListener;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author holger
 */
public class AddContractsAndLoopInvariantsListener extends Java8BaseListener {

    private StaticCGJavaMethod methodToDisprove;
    private String contract;
    private List<String> loopInvariants;
    private String loopInvariantTemplate;
    private Set<StaticCGJavaMethod> methodsInThisClass;
    private Map<StaticCGJavaMethod, String> methodsToMostGeneralContracts;

    private List<String> classCodeInLines;
    private Map<Integer, String> linePosToContentToCopy;
    private List<Integer> linePosOfContentToCopy;

    public String addContractsAndLoopInvariants(
            StaticCGJavaMethod methodToDisprove,
            String contract,
            List<String> loopInvariants,
            String loopInvariantTemplate,
            Set<StaticCGJavaMethod> methodsInThisClass,
            Map<StaticCGJavaMethod, String> methodsToMostGeneralContracts,
            String classCode) {
        this.methodToDisprove = methodToDisprove;
        this.contract = contract;
        this.loopInvariants = loopInvariants;
        this.loopInvariantTemplate = loopInvariantTemplate;
        this.methodsInThisClass = methodsInThisClass;
        this.methodsToMostGeneralContracts = methodsToMostGeneralContracts;

        linePosToContentToCopy = new HashMap<>();
        linePosOfContentToCopy = new ArrayList<>();

        this.classCodeInLines = CopyKeyCompatibleListener.seperateCodeIntoLines(classCode);

        Java8Lexer lexer = new Java8Lexer(new ANTLRInputStream(classCode));
        Java8Parser parser = new Java8Parser(new CommonTokenStream(lexer));
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, parser.compilationUnit());

        //by now we have what to put on the lines as well as which lines to put
        //it on in the linepostocontenttocopy map. Now to add it
        List<Integer> distanceBetweenLinesOfStringsToInsert = new ArrayList<>();
        if (linePosOfContentToCopy.size() > 0) {
            linePosOfContentToCopy.sort(Comparator.<Integer>naturalOrder());
            distanceBetweenLinesOfStringsToInsert.add(linePosOfContentToCopy.get(0));
            for (int i = 1; i < linePosOfContentToCopy.size(); ++i) {
                distanceBetweenLinesOfStringsToInsert.add(
                        linePosOfContentToCopy.get(i)
                        - linePosOfContentToCopy.get(i - 1));
            }
        }

        StringBuilder generatedCode = new StringBuilder();
        int linePos = 0;
        int posInLinePosList = 0;
        for (int currentDist : distanceBetweenLinesOfStringsToInsert) {
            while (currentDist > 1) {
                generatedCode.append(classCodeInLines.get(linePos)).append('\n');
                ++linePos;
                --currentDist;
            }
            generatedCode.append(linePosToContentToCopy.get(linePosOfContentToCopy.get(posInLinePosList))).append('\n');
            generatedCode.append(classCodeInLines.get(linePos)).append('\n');
            ++linePos;
            posInLinePosList++;
        }
        //just copy the rest
        while (linePos < classCodeInLines.size()) {
            generatedCode.append(classCodeInLines.get(linePos)).append('\n');
            linePos++;
        }
        return generatedCode.toString();
    }

    private void addStringForMethodOrCtor(StaticCGJavaMethod method, int startLine) {
        linePosOfContentToCopy.add(startLine);
        if (methodToDisprove.equals(method)) { //add the special contract and loop invariants
            linePosToContentToCopy.put(startLine, contract);
            int i = 0;
            for (int relLoopPos : methodToDisprove.getRelPosOfLoops()) {
                int absLinePos = startLine + relLoopPos;
                linePosOfContentToCopy.add(absLinePos);
                String loopInvariant = loopInvariants.get(i);
                if (loopInvariant == null) {
                    linePosToContentToCopy.put(absLinePos, loopInvariantTemplate);
                } else {
                    linePosToContentToCopy.put(absLinePos, loopInvariant);
                }
                ++i;
            }
        } else { //add only the most general contract
            linePosToContentToCopy.put(startLine, methodsToMostGeneralContracts.get(method));
        }
    }

    @Override
    public void enterConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx) {
        String id = "<init>";
        String args = GetMethodBodyListener.getArgTypeString(ctx.constructorDeclarator().formalParameterList());
        StaticCGJavaMethod method = CopyKeyCompatibleListener.findMethodInSet(methodsInThisClass, id, args);
        addStringForMethodOrCtor(method, ctx.getStart().getLine());
    }

    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        String id = ctx.methodHeader().methodDeclarator().Identifier().getText();
        String args = GetMethodBodyListener.getArgTypeString(ctx.methodHeader().methodDeclarator().formalParameterList());
        StaticCGJavaMethod method = CopyKeyCompatibleListener.findMethodInSet(methodsInThisClass, id, args);
        addStringForMethodOrCtor(method, ctx.getStart().getLine());
    }

}
