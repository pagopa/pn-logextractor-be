package it.gov.pagopa.logextractor.util.external.filestorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CustomFileOutputStream extends FileOutputStream{

	private String fileName;
	
	public CustomFileOutputStream(String filename) throws FileNotFoundException {
		super(filename+".tmp");
		this.fileName = filename;
	}

	@Override
	public void close() throws IOException {
		super.close();
		new File(fileName+".tmp").renameTo(new File(fileName));
	}
	

}
