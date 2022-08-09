package it.gov.pagopa.logextractor.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.dto.NotificationCsvBean;
import it.gov.pagopa.logextractor.dto.NotificationData;

/**
 * Utility class to manage the core operations for files
 */
public class FileUtilities {

	/**
	 * Create a new file with the given name
	 * @param name the name of the file to retrieve
	 * @return a new {@link File} instance of a file with the given name
	 * */
	public File getFile(String name, String extension) {
		return FileUtils.getFile(Constants.EXPORT_FOLDER + name + "-" +  new CommonUtilities().generateRandomToken() + extension);
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
	 * Delete the files of the input file list
	 * @param files The list of files to be deleted
	 * */
	public void deleteFiles(ArrayList<File> files) {
		for(File fileToDelete : files) {
			deleteFile(fileToDelete);
		}
	}
	
	/**
	 * Write notification data to a csv file
	 * @param file the file where to write the content into
	 * @param notifications the list of notifications
	 * @throws IOException in case of an I/O error
	 * @throws CsvDataTypeMismatchException If a field of the beans is annotated improperly or an unsupported data type is supposed to be written
	 * @throws CsvRequiredFieldEmptyException If a field is marked as required,but the source is null
	 * */
	public void writeCsv(File file, ArrayList<NotificationCsvBean> notifications) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		Writer writer = new FileWriter(file);
		StatefulBeanToCsv<NotificationCsvBean> beanToCsv = new StatefulBeanToCsvBuilder<NotificationCsvBean>(writer).withSeparator(CSVWriter.DEFAULT_SEPARATOR).build();
		beanToCsv.write(notifications);
		writer.close();
	}
	
	/**
	 * Converts notification data into csv data object
	 * @param notificationData the {@link NotificationData} object to convert
	 * @return A {@link NotificationCsvBean} object representing the converted input object
	 * */
	public NotificationCsvBean toCsv(NotificationData notificationData) {
		CommonUtilities commonUtils = new CommonUtilities();
		NotificationCsvBean notification = new NotificationCsvBean();
		if(null != notificationData.getRecipients() && notificationData.getRecipients().size() > 0) {
			StringBuilder recipientsBuilder = new StringBuilder();
			for(String tempRecipient : notificationData.getRecipients()) {
				recipientsBuilder.append(tempRecipient + "-");
			}
			recipientsBuilder.deleteCharAt(recipientsBuilder.length()-1);
			notification.setCodici_fiscali(commonUtils.escapeForCsv(recipientsBuilder.toString()));
			recipientsBuilder.setLength(0);
		}
		notification.setIUN(commonUtils.escapeForCsv(notificationData.getIun()));
		notification.setData_invio(commonUtils.escapeForCsv(notificationData.getSentAt()));
		notification.setData_generazione_attestazione_opponibile_a_terzi(
				commonUtils.escapeForCsv(notificationData.getRequestAcceptedAt()));
		notification.setOggetto(commonUtils.escapeForCsv(notificationData.getSubject()));
		return notification;
	}
	
	/**
	 * Converts a list of notification data into csv data objects
	 * @param notificationData the list of {@link NotificationData} objects to convert
	 * @return A list of {@link NotificationCsvBean} objects representing the converted input objects
	 * */
	public ArrayList<NotificationCsvBean> toCsv(List<NotificationData> notificationData) {
		ArrayList<NotificationCsvBean> csvNotifications = new ArrayList<>();
		for(NotificationData notification : notificationData) {
			csvNotifications.add(toCsv(notification));
		}
		return csvNotifications;
	}
}
