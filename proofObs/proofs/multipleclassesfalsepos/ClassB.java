package multipleclassesfalsepos;
public class ClassB{
public int[] arr;
	/*@ requires true;
	  @ determines this \by this.arr, this; */
    int[] putDataInArr(int high) {
     arr[4] = high;
        return arr;
    }

}
