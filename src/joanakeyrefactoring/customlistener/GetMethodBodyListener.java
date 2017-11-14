/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.customlistener;

import java.util.ArrayList;
import java.util.List;
import joanakeyrefactoring.antlr.java8.Java8BaseListener;
import joanakeyrefactoring.antlr.java8.Java8Lexer;
import joanakeyrefactoring.antlr.java8.Java8Parser;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author holger
 */
public class GetMethodBodyListener extends Java8BaseListener {

    private final static String NULLABLE = "/*@ nullable @*/ ";
    private final static String INIT = "<init>";
    private final static String ARGS = "ARGS";
    private List<String> extractedMethodParamNames;
    private boolean parsedRightMethod;
    //private String methodBody;
    private StaticCGJavaMethod method;
    private int methodStartLine;
    private String methodParamsNullable;
    private String methodDeclWithNullable;
    private int startLine;
    private int stopLine;

    public void parseFile(String file, StaticCGJavaMethod method) {
        Java8Lexer java8Lexer = new Java8Lexer(new ANTLRInputStream(file));
        Java8Parser java8Parser = new Java8Parser(new CommonTokenStream(java8Lexer));
        ParseTreeWalker walker = new ParseTreeWalker();
        extractedMethodParamNames = new ArrayList<>();
        this.method = method;
        parsedRightMethod = false;
        walker.walk(this, java8Parser.compilationUnit());
    }

    public List<String> getExtractedMethodParamNames() {
        return extractedMethodParamNames;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStopLine() {
        return stopLine;
    }

    public int getMethodStartLine() {
        return methodStartLine;
    }

    public String getMethodParamsNullable() {
        return methodParamsNullable;
    }

    public String getMethodDeclWithNullable() {
        return methodDeclWithNullable;
    }

    @Override
    public void enterConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx) {
        if (parsedRightMethod == true || !method.getId().equals(INIT)) {
            return;
        }
        String argTypeString = getArgTypeString(ctx.constructorDeclarator().formalParameterList());
        if (!argTypeString.equals(method.getParameterWithoutPackage())) {
            return;
        }

        startLine = ctx.constructorDeclarator().getStart().getLine();
        stopLine = ctx.constructorDeclarator().getStop().getLine();

        methodParamsNullable = "";
        methodDeclWithNullable =
                method.getContainingClass().getOnlyClassName() + "(" + ARGS + ")";

        getArgTypeStringWithNullable(ctx.constructorDeclarator().formalParameterList());

        parsedRightMethod = true;
        methodDeclWithNullable = methodDeclWithNullable.replace(ARGS, methodParamsNullable);
        if (parsedRightMethod == true) {
            methodStartLine = ctx.getStart().getLine();
            List<Java8Parser.ConstructorModifierContext> ctorModifier = ctx.constructorModifier();
            String methodMods = "";
            for (Java8Parser.ConstructorModifierContext mod : ctorModifier) {
                methodMods += mod.getText() + " ";
            }
            methodDeclWithNullable = methodMods + " " + methodDeclWithNullable;
        }
    }

    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        if (parsedRightMethod == true) {
            return;
        }
        parseMethodDeclarator(ctx.methodHeader().methodDeclarator());
        if (parsedRightMethod == true) {
            methodStartLine = ctx.getStart().getLine();
            List<Java8Parser.MethodModifierContext> methodModifier = ctx.methodModifier();
            String methodMods = "";
            for (Java8Parser.MethodModifierContext mod : methodModifier) {
                methodMods += mod.getText() + " ";
            }
            String type = ctx.methodHeader().result().getText();
            methodDeclWithNullable = methodMods + type + " " + methodDeclWithNullable;
        }
    }

    public void parseMethodDeclarator(Java8Parser.MethodDeclaratorContext ctx) {
        //String text =
        ctx.getText();
        String methodName = ctx.Identifier().getText();
        if (!methodName.equals(method.getId())) {
            return;
        }
        Java8Parser.FormalParameterListContext formalParameterList = ctx.formalParameterList();
        String argTypeString = getArgTypeString(formalParameterList);

        //Problem: The method has the params with the package decl, the listener however does not
        //current workaround: compare to params wo package decl
        if (!argTypeString.equals(method.getParameterWithoutPackage())) {
            return;
        }

        startLine = ctx.getStart().getLine();
        stopLine = ctx.getStop().getLine();

        methodParamsNullable = "";
        methodDeclWithNullable = methodName + "(" + ARGS + ")";

        getArgTypeStringWithNullable(formalParameterList);

        parsedRightMethod = true;
        methodDeclWithNullable = methodDeclWithNullable.replace(ARGS, methodParamsNullable);
    }

    private void getArgTypeStringWithNullable(Java8Parser.FormalParameterListContext ctx) {
        if (ctx != null) {
            Java8Parser.FormalParametersContext formalParameters = ctx.formalParameters();
            if (formalParameters != null) {
                for (Java8Parser.FormalParameterContext currentFormalParam
                        : formalParameters.formalParameter()) {
                    String variableDeclaratorId = currentFormalParam.variableDeclaratorId().getText();
                    extractedMethodParamNames.add(variableDeclaratorId);
                    String varType = currentFormalParam.unannType().getText();
                    methodParamsNullable += varType + NULLABLE + variableDeclaratorId + ", ";
                }
            }
            Java8Parser.LastFormalParameterContext lastFormalParameter = ctx.lastFormalParameter();
            String variableDeclaratorId =
                    lastFormalParameter.formalParameter().variableDeclaratorId().getText();
            extractedMethodParamNames.add(variableDeclaratorId);
            String varType = lastFormalParameter.formalParameter().unannType().getText();
            methodParamsNullable += varType + NULLABLE + variableDeclaratorId;
        }
    }

    public static String getArgTypeString(Java8Parser.FormalParameterListContext ctx) {
        if (ctx == null) {
            return "";
        }
        String created = "";
        Java8Parser.FormalParametersContext formalParameters = ctx.formalParameters();
        if (formalParameters != null) {
            for (Java8Parser.FormalParameterContext currentFormalParam
                    : formalParameters.formalParameter()) {
                Java8Parser.UnannTypeContext unannType = currentFormalParam.unannType();
                String currentTypeString = unannType.getText();
                created += currentTypeString + ",";
            }
        }
        Java8Parser.LastFormalParameterContext lastFormalParameter = ctx.lastFormalParameter();
        created += lastFormalParameter.formalParameter().unannType().getText();
        return created;
    }

}
