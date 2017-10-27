/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multipleclassesfalsepos;

/**
 *
 * @author holger
 */
public class ClassB {
    public int[] arr;   
    ClassB(int x) {
        this.arr = new int[3];
        this.arr[2] = x;
    }
    
    ClassB() {}
    
    int[] putDataInArr(int high) {
        if (arr != null && 4 < arr.length) {
            arr[4] = high;
        }
        return arr;
    }
    
}
