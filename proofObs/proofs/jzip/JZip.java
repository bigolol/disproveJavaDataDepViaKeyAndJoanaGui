package jzip;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
public class JZip{
private boolean run;
private String[] args;
private String commandline = "";
	/*@ requires this != sourceFolder && sourceFolder != this && file != this && this != file;
	  @ determines \result \by this, file, <exception>cause.detailMessage, file.value; */
	private String generateZipEntry(String file, String sourceFolder) {
		return file.substring(sourceFolder.length(), file.length());
	}

}
