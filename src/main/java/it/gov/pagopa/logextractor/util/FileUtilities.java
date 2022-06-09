package it.gov.pagopa.logextractor.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.dto.NotificationCsvBean;

/**
 * Utility class to manage the core operations for files
 */
public class FileUtilities {

	/**
	 * Create a new file with the given name
	 * @param name the name of the file to retrieve
	 * @return a new instance of a file with the given name
	 * */
	public File getFile(String name, String extension) {
		return FileUtils.getFile(Constants.EXPORT_FOLDER + name + "-" +  new RandomGenerator().generateRandomToken() + extension);
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
	
	/**
	 * Write the input content list into the input file
	 * @param file the file where to write the content into
	 * @param content the content list to write into the file
	 * @throws IOException in case of an I/O error
	 * */
	public void write(File file, ArrayList<String> contents) throws IOException {
		if(contents != null) {
			for(String contentTemp : contents) {
				write(file, contentTemp+"\n");
			}
		}
	}	
	
	/**
	 * Clean up the input file
	 * @param file The file to clean
	 * @throws IOException in case of an I/O error
	 * */
	public void cleanFile(File file) throws IOException {
		FileUtils.writeStringToFile(file, "", StandardCharsets.UTF_8);
	}
	
	/**
	 * Delete an existing file
	 * @param file The file to be deleted
	 * */
	public void deleteFile(File file) {
		file.delete();
	}
	
	/**
	 * Write notification data to a csv file
	 * @param file the file where to write the content into
	 * @param notifications the list of notifications
	 * @throws IOException in case of an I/O error
	 * @throws CsvDataTypeMismatchException If a field of the beans isannotated improperly or an unsupported data type is supposed to bewritten
	 * @throws CsvRequiredFieldEmptyException If a field is marked as required,but the source is null
	 * */
	public void writeCsv(File file, ArrayList<NotificationCsvBean> notifications) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
		Writer writer = new FileWriter(file);
		StatefulBeanToCsv<NotificationCsvBean> beanToCsv = new StatefulBeanToCsvBuilder<NotificationCsvBean>(writer).withSeparator(CSVWriter.DEFAULT_SEPARATOR).build();
		beanToCsv.write(notifications);
		writer.close();
	}
	
	
}
