/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.loopinvarianthandling;

/**
 *
 * @author holger
 */
public class LoopInvariant {
    private int posOfLoopInMethod;
    private String invariantString;

    public LoopInvariant(int posOfLoopInMethod, String invariantString) {
        this.posOfLoopInMethod = posOfLoopInMethod;
        this.invariantString = invariantString;
    }
    
}
