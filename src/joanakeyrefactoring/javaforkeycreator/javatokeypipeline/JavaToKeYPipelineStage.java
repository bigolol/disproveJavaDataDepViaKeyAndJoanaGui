/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator.javatokeypipeline;

import java.util.Set;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public interface JavaToKeYPipelineStage {
    String transformCode(String code, Set<StaticCGJavaMethod> neededMethods);
}
