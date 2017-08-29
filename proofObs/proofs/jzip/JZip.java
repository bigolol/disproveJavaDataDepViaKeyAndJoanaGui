package jzip;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
public class JZip{
/*@spec_public@*/private boolean run;
/*@spec_public@*/private String[] args;
/*@spec_public@*/private String commandline = "";
	/*@ requires this != sourceFolder && sourceFolder != this && file != this && this != file;
	  @ determines this \by this, file, sourceFolder, file.value; */
	private String generateZipEntry(String file, String sourceFolder) {
		return file.substring(sourceFolder.length(), file.length());
	}

}
