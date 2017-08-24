package jzip;

public class MyFileOutputStream {
	private byte[] location = new byte[512]; // high input
	private byte[] content = new byte[512]; // low output

	public MyFileOutputStream() {
	}

	// determines content by input[0..255]
	public void write(byte[] input) {
		for (int i = 0; i < input.length / 2; i++) {

			content[i] = input[i];

			location[i] = input[i + input.length / 2];

		}
	}
}
