/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring;

import edu.kit.joana.api.sdg.SDGProgramPart;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 *
 * @author holger
 */
public class SingleAnnotationAdder {

    private Supplier<Collection<SDGProgramPart>> programPartSupplier;
    private BiConsumer<SDGProgramPart, String> annoAddMethod;
    private String secLevel;

    public SingleAnnotationAdder(Supplier<Collection<SDGProgramPart>> programPartSupplier, BiConsumer<SDGProgramPart, String> annoAddMethod, String secLevel) {
        this.programPartSupplier = programPartSupplier;
        this.annoAddMethod = annoAddMethod;
        this.secLevel = secLevel;
    }

    public void addYourselfToAnalysis() {
        programPartSupplier.get().forEach((part) -> {
            annoAddMethod.accept(part, secLevel);
        });
    }
}
