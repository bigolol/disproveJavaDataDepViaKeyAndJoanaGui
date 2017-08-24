public class PlusMinusFalsePos {
	public static void main(String[] args) {
		new PlusMinusFalsePos().testMethod(1, 2);
	}

	public int testMethod(int high, int low) {
		int one = identity(low, high);
		return one;
	}

	public int identity(int l, int h) {
		l = l + h;
		l = l - h;
		return l;
	}
}
