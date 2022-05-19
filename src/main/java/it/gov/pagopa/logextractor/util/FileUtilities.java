package it.gov.pagopa.logextractor.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

/**
 * Utility class to manage the core operations for files
 */
public class FileUtilities {

	/**
	 * Create a new file with the given name
	 * @param name the name of the file to retrieve
	 * @return a new instance of a file with the given name
	 * */
	public File getFile(String name) {
		return FileUtils.getFile(name);
	}
	
	/**
	 * Write the input content into the input file
	 * @param file the file where to write the content into
	 * @param content the content to write into the file
	 * @throws IOException in case of an I/O error
	 * */
	public void write(File file, String content) throws IOException {
		FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8, true);
	}
}
