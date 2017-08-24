/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator;

import java.util.ArrayList;
import java.util.List;
import joanakeyrefactoring.antlr.java8.Java8BaseListener;
import joanakeyrefactoring.antlr.java8.Java8Lexer;
import joanakeyrefactoring.antlr.java8.Java8Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author holger
 */
public class LoopListener extends Java8BaseListener {

    private List<Integer> created = new ArrayList<>();

    public List<Integer> findLoopLines(String code) {
        created = new ArrayList<>();
        Java8Lexer l = new Java8Lexer(new ANTLRInputStream(code));
        Java8Parser p = new Java8Parser(new CommonTokenStream(l));
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, p.compilationUnit());
        return created;
    }

    @Override
    public void enterForStatement(Java8Parser.ForStatementContext ctx) {
        created.add(ctx.start.getStartIndex());
        created.add(ctx.stop.getStopIndex());
    }

    @Override
    public void enterWhileStatement(Java8Parser.WhileStatementContext ctx) {
        created.add(ctx.start.getStartIndex());
        created.add(ctx.stop.getStopIndex());
    }

    @Override
    public void enterDoStatement(Java8Parser.DoStatementContext ctx) {
        created.add(ctx.start.getStartIndex());
        created.add(ctx.stop.getStopIndex());
    }
}
