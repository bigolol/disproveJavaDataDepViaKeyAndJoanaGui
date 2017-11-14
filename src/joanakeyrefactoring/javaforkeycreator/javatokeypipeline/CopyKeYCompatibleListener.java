/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator.javatokeypipeline;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import joanakeyrefactoring.ViolationsWrapper;
import joanakeyrefactoring.antlr.java8.Java8BaseListener;
import joanakeyrefactoring.antlr.java8.Java8Lexer;
import joanakeyrefactoring.antlr.java8.Java8Parser;
import joanakeyrefactoring.customlistener.GetMethodBodyListener;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author holger
 */
public class CopyKeYCompatibleListener extends Java8BaseListener implements JavaToKeYPipelineStage {

    private final static String PUBLIC = "public";
    private final static String SPEC_PUBLIC = "/*@spec_public@*/";
    private StringBuilder currentlyGenerated;
    private List<String> keyCompatibleJavaFeature = new ArrayList<>();
    private List<String> classCodeAsLines = new ArrayList<>();
    private List<String> importStatements = new ArrayList<>();
    private String mainPackageName;
    private String packageOfClass;
    Set<StaticCGJavaMethod> neededMethods;

    public CopyKeYCompatibleListener(String mainPackageName) throws FileNotFoundException, IOException {
        InputStream is = new FileInputStream(ViolationsWrapper.KeY_COMPATIBLE_JAVA_FEATURES);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        this.mainPackageName = mainPackageName;
        while (line != null) {
            keyCompatibleJavaFeature.add(line);
            line = buf.readLine();
        }
        buf.close();
    }

    private List<String> getPossiblePackagesForType(String type) {
        List<String> created = new ArrayList<>();
        for (String currentImport : importStatements) {
            int lastIndexOfDot = currentImport.lastIndexOf(".");
            String importedType = currentImport.substring(lastIndexOfDot + 1, currentImport.length());
            if (importedType.equals(type)) {
                created.add(currentImport);
            }
        }

        if (created.isEmpty()) {
            created.add(packageOfClass + "." + type);
            for (String currentImport : importStatements) {
                if (currentImport.endsWith(".*")) {
                    created.add(currentImport);
                }
            }
        }

        return created;
    }

    private boolean isTypeKeyCompatible(String type) {
        List<String> possiblePackagesForType = getPossiblePackagesForType(type);

        if (possiblePackagesForType.size() == 1) {
            if (isImportKeyCompatible(possiblePackagesForType.get(0))) {
                return true;
            }
        } else {
            for (String possImport : possiblePackagesForType) {
                if (!isImportKeyCompatible(possImport)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isImportKeyCompatible(String importStmt) {
        int firstDot = importStmt.indexOf(".");
        String mainPackageOfImportStmt = importStmt.substring(0, firstDot);
        if (mainPackageOfImportStmt.equals(mainPackageName)) {
            return true;
        }
        for (String s : keyCompatibleJavaFeature) {
            if (s.equals(importStmt)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void enterConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx) {
        if (isCtorNecessary(ctx)) {
            currentlyGenerated.append(extractStringInBetween(ctx, classCodeAsLines)).append("\n");
        }
    }

    private boolean isCtorNecessary(Java8Parser.ConstructorDeclarationContext ctx) {
        String id = "<init>";
        String args = GetMethodBodyListener.getArgTypeString(ctx.constructorDeclarator().formalParameterList());
        return isMethodNeeded(id, args);
    }

    public static StaticCGJavaMethod findMethodInSet(Set<StaticCGJavaMethod> methods, String id, String args) {
        for (StaticCGJavaMethod m : methods) {
            if (m.getId().equals(id) && m.getParameterWithoutPackage().equals(args)) {
                return m;
            }
        }
        return null;
    }

    private boolean isMethodNeeded(String methodId, String args) {
        StaticCGJavaMethod m = findMethodInSet(neededMethods, methodId, args);
        if (m == null) {
            return false;
        }
        return true;
    }

    @Override
    public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        String classDecl = "";
        List<Java8Parser.ClassModifierContext> classModifier = ctx.normalClassDeclaration().classModifier();
        for (Java8Parser.ClassModifierContext modCtx : classModifier) {
            classDecl += modCtx.getText() + " ";
        }
        classDecl += "class ";
        String identifier = ctx.normalClassDeclaration().Identifier().getText();
        classDecl += identifier;
        currentlyGenerated.append(classDecl).append("{\n");
    }

    @Override
    public void exitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        currentlyGenerated.append("}\n");
    }

    public static String extractStringInBetween(ParserRuleContext ctx, List<String> classCodeAsLines) {
        int startLine = ctx.getStart().getLine();
        int startCharPositionInLine = ctx.getStart().getCharPositionInLine();
        int stopLine = ctx.getStop().getLine();
        int stopCharPositionInLine = ctx.getStop().getCharPositionInLine();

        String s = "";
        if (startLine == stopLine) {
            return classCodeAsLines.get(startLine - 1).substring(startCharPositionInLine, stopCharPositionInLine);
        }
        for (int i = startLine - 1; i < stopLine; ++i) {
            if (i == startLine) {
                s += classCodeAsLines.get(i).substring(startCharPositionInLine - 1, classCodeAsLines.get(i).length()) + '\n';
            } else if (i == stopLine) {
                s += classCodeAsLines.get(i).substring(0, stopCharPositionInLine - 1);
            } else {
                s += classCodeAsLines.get(i) + '\n';
            }
        }

        return s;
    }

    @Override
    public void enterPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {
        List<TerminalNode> Identifier = ctx.Identifier();
        packageOfClass = "";
        for (TerminalNode n : Identifier) {
            packageOfClass += n.getText() + ".";
        }
        packageOfClass = packageOfClass.substring(0, packageOfClass.length() - 1);
        currentlyGenerated.append(extractStringInBetween(ctx, classCodeAsLines)).append(";\n");
    }

    @Override
    public void enterImportDeclaration(Java8Parser.ImportDeclarationContext ctx) {
        String importStmt = ctx.getText().substring("import".length(), ctx.getText().length() - 1);
        importStatements.add(importStmt);
        if (isImportKeyCompatible(importStmt)) {
            currentlyGenerated.append(extractStringInBetween(ctx, classCodeAsLines)).append(";\n");
        }
    }

    @Override
    public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        String type = ctx.unannType().getText();
        int lastIndexOfLessThan = type.lastIndexOf("<");
        if (lastIndexOfLessThan != -1) {
            type = type.substring(0, lastIndexOfLessThan);
            return;
        }
        if (isTypeKeyCompatible(type)) {
            List<Java8Parser.FieldModifierContext> fieldModifier = ctx.fieldModifier();
            for (Java8Parser.FieldModifierContext currentMod : fieldModifier) {
                if (currentMod.getText().equals(PUBLIC)) {
                    currentlyGenerated.append(extractStringInBetween(ctx, classCodeAsLines)).append(";\n");
                    return;
                }
            }
            currentlyGenerated
                    .append(SPEC_PUBLIC)
                    .append(extractStringInBetween(ctx, classCodeAsLines)).append(";\n");
        }
    }

    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        if (methodIsNeeded(ctx)) {
            currentlyGenerated.append(extractStringInBetween(ctx, classCodeAsLines)).append('\n');
        }
    }

    private boolean methodIsNeeded(Java8Parser.MethodDeclarationContext ctx) {
        String id = ctx.methodHeader().methodDeclarator().Identifier().getText();
        String args = GetMethodBodyListener.getArgTypeString(ctx.methodHeader().methodDeclarator().formalParameterList());
        return isMethodNeeded(id, args);
    }

    public static List<String> seperateCodeIntoLines(String code) {
        List<String> generated = new ArrayList<>();
        String[] split = code.split("\n");
        for (int i = 0; i < split.length; i++) {
            String string = split[i];
            generated.add(string);
        }
        return generated;
    }

    public String transformCode(String classCode, Set<StaticCGJavaMethod> neededMethods) {
        currentlyGenerated = new StringBuilder();
        classCodeAsLines = new ArrayList<>();
        importStatements = new ArrayList<>();
        this.neededMethods = neededMethods;
        classCodeAsLines = seperateCodeIntoLines(classCode);
        Java8Lexer java8Lexer = new Java8Lexer(new ANTLRInputStream(classCode));
        Java8Parser java8Parser = new Java8Parser(new CommonTokenStream(java8Lexer));
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, java8Parser.compilationUnit());

        return currentlyGenerated.toString();
    }

}
