package it.gov.pagopa.logextractor.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.opencsv.ICSVWriter;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import org.apache.commons.io.FileUtils;
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
		return FileUtils.getFile(GenericConstants.EXPORT_FOLDER + name + "-" +  new RandomUtils().generateRandomAlphaNumericString() + extension);
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
	 * @param contents the content list to write into the file
	 * @throws IOException in case of an I/O error
	 * */
	public void write(File file, List<String> contents) throws IOException {
		if(contents != null) {
			for(String contentTemp : contents) {
				write(file, contentTemp+"\n");
			}
		}
	}
	
	/**
	 * Delete an existing file
	 * @param file The file to be deleted
	 * */
	public void deleteFile(File file) throws LogExtractorException {

		boolean fileHasBeenDeleted = file.delete();
		if(!fileHasBeenDeleted){
			throw new LogExtractorException("Exception in file elimination, could not delete file: " + file.getName());
		}
	}
	
	/**
	 * Delete the files of the input file list
	 * @param files The list of files to be deleted
	 * */
	public void deleteFiles(List<File> files) throws LogExtractorException {
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
	public void writeCsv(File file, List<NotificationCsvBean> notifications) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		Writer writer = new FileWriter(file);
		StatefulBeanToCsv<NotificationCsvBean> beanToCsv = new StatefulBeanToCsvBuilder<NotificationCsvBean>(writer).withSeparator(ICSVWriter.DEFAULT_SEPARATOR).build();
		beanToCsv.write(notifications);
		writer.close();
	}
	
	/**
	 * Converts notification data into csv data object
	 * @param notificationData the {@link NotificationData} object to convert
	 * @return A {@link NotificationCsvBean} object representing the converted input object
	 * */
	public NotificationCsvBean toCsv(NotificationData notificationData) {
		EscapeUtils escapeUtils = new EscapeUtils();
		NotificationCsvBean notification = new NotificationCsvBean();
		if(null != notificationData.getRecipients() && !notificationData.getRecipients().isEmpty()) {
			StringBuilder recipientsBuilder = new StringBuilder();
			for(String tempRecipient : notificationData.getRecipients()) {
				recipientsBuilder.append(tempRecipient + "-");
			}
			recipientsBuilder.deleteCharAt(recipientsBuilder.length()-1);
			notification.setCodici_fiscali(escapeUtils.escapeForCsv(recipientsBuilder.toString()));
			recipientsBuilder.setLength(0);
		}
		notification.setIUN(escapeUtils.escapeForCsv(notificationData.getIun()));
		notification.setData_invio(escapeUtils.escapeForCsv(notificationData.getSentAt()));
		notification.setData_generazione_attestazione_opponibile_a_terzi(
				escapeUtils.escapeForCsv(notificationData.getRequestAcceptedAt()));
		notification.setOggetto(escapeUtils.escapeForCsv(notificationData.getSubject()));
		return notification;
	}
	
	/**
	 * Converts a list of notification data into csv data objects
	 * @param notificationData the list of {@link NotificationData} objects to convert
	 * @return A list of {@link NotificationCsvBean} objects representing the converted input objects
	 * */
	public List<NotificationCsvBean> toCsv(List<NotificationData> notificationData) {
		ArrayList<NotificationCsvBean> csvNotifications = new ArrayList<>();
		for(NotificationData notification : notificationData) {
			csvNotifications.add(toCsv(notification));
		}
		return csvNotifications;
	}
}
