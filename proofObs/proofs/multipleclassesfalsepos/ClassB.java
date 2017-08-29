package multipleclassesfalsepos;
public class ClassB{
public int[] arr;
	/*@ requires true;
	  @ determines this \by this, this.arr; */
    int[] putDataInArr(int high) {
     arr[4] = high;
        return arr;
    }

}
