/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.javaforkeycreator;

/**
 *
 * @author holger
 */
public class LoopInvariant {

    private String invariant;
    private int startChar;
    private int stopChar;

    public LoopInvariant(String invariant, int startChar, int stopChar) {
        this.invariant = invariant;
        this.startChar = startChar;
        this.stopChar = stopChar;
    }

    public String getInvariant() {
        return invariant;
    }

    public int getStartChar() {
        return startChar;
    }

    public int getStopChar() {
        return stopChar;
    }

}
