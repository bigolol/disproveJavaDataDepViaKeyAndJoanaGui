
import joanakeyrefactoring.antlr.java8.Java8Lexer;
import joanakeyrefactoring.antlr.java8.Java8Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author holger
 */
public class JavaParserTest {

    public static void main(String[] args) {
        String code = "class A { void func(){ while(true){} for(;;){} do {} while(true);} }";
        Java8Lexer lexer = new Java8Lexer(new ANTLRInputStream(code));
        Java8Parser parser = new Java8Parser(new CommonTokenStream(lexer));
        ParseTree tree = parser.compilationUnit();
        System.out.println(tree.toStringTree(parser));
    }
}
