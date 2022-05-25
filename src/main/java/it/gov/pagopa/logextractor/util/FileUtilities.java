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