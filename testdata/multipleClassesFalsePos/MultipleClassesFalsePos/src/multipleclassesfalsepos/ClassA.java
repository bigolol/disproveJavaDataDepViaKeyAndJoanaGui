/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multipleclassesfalsepos;

/**
 *
 * @author holgerklein
 */
public class ClassA {

    ClassB b = new ClassB();
    ClassB c = new ClassB();

    ClassA() {
    }

    public int falsePos(int high) {
        b.arr = new int[5];
        b.arr[0] = 1;
        c.arr = new int[3];
        c.arr[0] = 2;
        b.arr[1] = c.arr[0];
        return b.putDataInArr(high)[0];
    }
}
