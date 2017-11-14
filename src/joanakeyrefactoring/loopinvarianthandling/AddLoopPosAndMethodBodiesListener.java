/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.loopinvarianthandling;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import joanakeyrefactoring.CustomListener.GetMethodBodyListener;
import joanakeyrefactoring.antlr.java8.Java8BaseListener;
import joanakeyrefactoring.antlr.java8.Java8Lexer;
import joanakeyrefactoring.antlr.java8.Java8Parser;
import joanakeyrefactoring.javaforkeycreator.javatokeypipeline.CopyKeYCompatibleListener;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaClass;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author holger
 */
public class AddLoopPosAndMethodBodiesListener extends Java8BaseListener {

    private Collection<StaticCGJavaMethod> currentmethods;
    private StaticCGJavaMethod currentlyEnteredMethod;
    private int startLinePosOfMethod;
    private List<String> currentClassFileContents;

    public void findAllLoopsAndAddToMethods(
            String classFileContents, 
            Collection<StaticCGJavaMethod> methods,
            StaticCGJavaClass javaclass) {
        currentmethods = methods;
        currentClassFileContents = Arrays.asList(classFileContents.split("\n"));
        
        Java8Lexer lexer = new Java8Lexer(new ANTLRInputStream(classFileContents));
        Java8Parser parser = new Java8Parser(new CommonTokenStream(lexer));
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, parser.compilationUnit());
    }

    private StaticCGJavaMethod getStaticCGMethodAndInsertBody(String id, String args,
                                                              ParserRuleContext ctx) {
        for (StaticCGJavaMethod m : currentmethods) {
            if (m.getId().equals(id) && m.getParameterWithoutPackage().equals(args)) {
                m.setMethodBody(
                        CopyKeYCompatibleListener.extractStringInBetween(ctx,
                                                                         currentClassFileContents));
                return m;
            }
        }
        return null;
    }

    private StaticCGJavaMethod getMethodCorresToMethodDecl(Java8Parser.MethodDeclarationContext ctx) {
        String id = ctx.methodHeader().methodDeclarator().Identifier().getText();
        String args =
                GetMethodBodyListener.getArgTypeString(
                        ctx.methodHeader().methodDeclarator().formalParameterList());
        return getStaticCGMethodAndInsertBody(id, args, ctx);
    }

    private StaticCGJavaMethod getMethodCorresToCtor(Java8Parser.ConstructorDeclarationContext ctx) {
        String id = "<init>";
        String args =
                GetMethodBodyListener.getArgTypeString(
                        ctx.constructorDeclarator().formalParameterList());
        return getStaticCGMethodAndInsertBody(id, args, ctx);
    }

    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        StaticCGJavaMethod methodCorresToMethodDecl = getMethodCorresToMethodDecl(ctx);
        currentlyEnteredMethod = methodCorresToMethodDecl;
        startLinePosOfMethod = ctx.getStart().getLine();
    }

    @Override
    public void enterConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx) {
        StaticCGJavaMethod methodCorresToCtor = getMethodCorresToCtor(ctx);      
        currentlyEnteredMethod = methodCorresToCtor;
        startLinePosOfMethod = ctx.getStart().getLine();
    }

    private void addToCurrentMethod(ParserRuleContext ctx) {
        if (currentlyEnteredMethod == null) {
            return;
        }
        int absStart = ctx.getStart().getLine();
        int relStart = absStart - startLinePosOfMethod;
        
        currentlyEnteredMethod.addRelativeLoopLinePos(relStart);
    }

    @Override
    public void enterForStatement(Java8Parser.ForStatementContext ctx) {
        addToCurrentMethod(ctx);
    }

    @Override
    public void enterWhileStatement(Java8Parser.WhileStatementContext ctx) {
        addToCurrentMethod(ctx);
    }

    @Override
    public void enterDoStatement(Java8Parser.DoStatementContext ctx) {
        addToCurrentMethod(ctx);
    }

}
