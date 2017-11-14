/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator.javatokeypipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import joanakeyrefactoring.staticCG.javamodel.StaticCGJavaMethod;

/**
 *
 * @author holger
 */
public class JavaToKeYPipeline {
    
    private List<JavaToKeYPipelineStage> stages = new ArrayList<>();
    
    public void addStage(JavaToKeYPipelineStage stage) {
        stages.add(stage);
    }
    
    public String transformCode(String code, Set<StaticCGJavaMethod> neededMethods) {
        for(JavaToKeYPipelineStage s : stages) {
            code = s.transformCode(code, neededMethods);
        }
        return code;
    }
}
