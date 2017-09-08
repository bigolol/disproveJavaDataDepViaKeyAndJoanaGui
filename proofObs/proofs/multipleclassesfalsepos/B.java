package multipleclassesfalsepos;
public class B{
public int[] arr;
	/*@ requires true;
	  @ determines this \by this, this.arr; */
    int[] putDataInArr(int high) {
     arr[4] = high;
        return arr;
    }

}
