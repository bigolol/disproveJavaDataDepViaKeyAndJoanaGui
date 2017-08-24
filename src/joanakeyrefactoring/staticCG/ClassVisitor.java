/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package joanakeyrefactoring.staticCG;

import java.util.ArrayList;
import java.util.List;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaClass;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

/**
 * The simplest of class visitors, invokes the method visitor class for each
 * method found.
 */
public class ClassVisitor extends EmptyVisitor {

    private JavaClass clazz;
    private ConstantPoolGen constants;
    private String classReferenceFormat;
    private List<StaticCGJavaClass> referencedClasses = new ArrayList<>();
    private List<StaticCGJavaMethod> containedMethods = new ArrayList<>();
    private List<StaticCGJavaMethod> calledMethods = new ArrayList<>();
    private StaticCGJavaClass visitedClass;

    private OrderedHashSet<StaticCGJavaClass> alreadyFoundClasses;
    private OrderedHashSet<StaticCGJavaMethod> alreadyFoundMethods;

    public ClassVisitor(JavaClass jc) {
        clazz = jc;
        constants = new ConstantPoolGen(clazz.getConstantPool());
        classReferenceFormat = "C:" + clazz.getClassName() + " %s";
    }

    public void visitJavaClass(JavaClass jc) {
        jc.getConstantPool().accept(this);
        Method[] methods = jc.getMethods();
        for (int i = 0; i < methods.length; i++) {
            methods[i].accept(this);
        }
    }

    public void visitConstantPool(ConstantPool constantPool) {
        for (int i = 0; i < constantPool.getLength(); i++) {
            Constant constant = constantPool.getConstant(i);
            if (constant == null) {
                continue;
            }
            if (constant.getTag() == 7) {
                String referencedClass
                        = constantPool.constantToString(constant);
                System.out.println(String.format(classReferenceFormat,
                        referencedClass));
                StaticCGJavaClass staticCGRefJavaClass = new StaticCGJavaClass(referencedClass);
                if (!alreadyFoundClasses.contains(staticCGRefJavaClass)) {
                    alreadyFoundClasses.add(staticCGRefJavaClass);
                    visitedClass.addReferencedClass(staticCGRefJavaClass);
                } else {
                    StaticCGJavaClass alreadyExistingRefClass
                            = findEntryInSet(alreadyFoundClasses, staticCGRefJavaClass);
                    visitedClass.addReferencedClass(alreadyExistingRefClass);
                }
                referencedClasses.add(staticCGRefJavaClass);
            }
        }
    }

    public void visitMethod(Method method) {
        MethodGen mg = new MethodGen(method, clazz.getClassName(), constants);
        mg.getReturnType().toString();
        
        StaticCGJavaMethod visitedMethod = new StaticCGJavaMethod(
                visitedClass, mg.getName(),
                MethodVisitor.argumentList(mg.getArgumentTypes()),
                mg.isStatic(), mg.getReturnType().toString());
        if (alreadyFoundMethods.contains(visitedMethod)) {
            visitedMethod = findEntryInSet(alreadyFoundMethods, visitedMethod);
            visitedMethod.setIsStatic(mg.isStatic());
        } else {
            alreadyFoundMethods.add(visitedMethod);
        }
        visitedClass.addContainedMethod(visitedMethod);
        MethodVisitor visitor = new MethodVisitor(mg, clazz);
        visitor.start();
        for (StaticCGJavaMethod m : visitor.getReferencedMethods()) {
            if (!alreadyFoundMethods.contains(m)) {
                StaticCGJavaClass containingClass = m.getContainingClass();
                if (!alreadyFoundClasses.contains(containingClass)) {
                    alreadyFoundClasses.add(containingClass);
                } else {
                    containingClass = findEntryInSet(alreadyFoundClasses, containingClass);
                    m.setContainingClass(containingClass);
                }
                alreadyFoundMethods.add(m);
                visitedMethod.addCalledMethod(m);
            } else {
                StaticCGJavaMethod foundMethod = findEntryInSet(alreadyFoundMethods, m);
                visitedMethod.addCalledMethod(foundMethod);
            }
        }
    }

    public void start(OrderedHashSet<StaticCGJavaClass> alreadyFoundClasses,
            OrderedHashSet<StaticCGJavaMethod> alreadyFoundMethods) {
        this.alreadyFoundClasses = alreadyFoundClasses;
        this.alreadyFoundMethods = alreadyFoundMethods;
        visitedClass = new StaticCGJavaClass(clazz.getClassName());
        if (!alreadyFoundClasses.contains(visitedClass)) {
            alreadyFoundClasses.add(visitedClass);
        } else {
            StaticCGJavaClass alreadyFoundClass = findEntryInSet(alreadyFoundClasses, visitedClass);
            visitedClass = alreadyFoundClass;
        }
        visitJavaClass(clazz);
    }

    private <A> A findEntryInSet(OrderedHashSet<A> set, A searchedEntry) {
        for (A a : set) {
            if (a.equals(searchedEntry)) {
                return a;
            }
        }
        return null;
    }

    public StaticCGJavaClass getVisitedClass() {
        return visitedClass;
    }
    
    
}
