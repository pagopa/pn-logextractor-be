package it.gov.pagopa.logextractor.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.NotificationCsvBean;
import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;

/**
 * Utility class to manage the core operations for files
 */
@Component
public class FileUtilities {

	private String exportFolder;

	public FileUtilities(@Value("${export.folder:${java.io.tmpdir}}") String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	/**
	 * Create a new file with the given name plus a random alphanumeric string and
	 * the given extension
	 * 
	 * @param name      the name of the file to retrieve
	 * @param extension the file extension
	 * @return a new {@link File} instance of a file with the given name
	 */
	public File getFileWithRandomName(String name, String extension) {
		return FileUtils.getFile(exportFolder,
				name + "-" + new RandomUtils().generateRandomAlphaNumericString() + extension);
	}

	/**
	 * Create a new file with the given name and the given extension
	 * 
	 * @param name      the name of the file to retrieve
	 * @param extension the file extension
	 * @return a new {@link File} instance of a file with the given name
	 */
	public File getFile(String name, String extension, String url) throws IOException {
		File downloadedFile = FileUtils.getFile(exportFolder, name + extension);
		FileUtils.copyURLToFile(new URL(url), downloadedFile);
		return downloadedFile;
	}

	/**
	 * Write the input content into the input file
	 * 
	 * @param file    the file where to write the content into
	 * @param content the content to write into the file
	 * @throws IOException in case of an I/O error
	 */
	public void write(File file, String content) throws IOException {
		FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8, true);
	}

	/**
	 * Write the input content list into the input file
	 * 
	 * @param file     the file where to write the content into
	 * @param contents the content list to write into the file
	 * @throws IOException in case of an I/O error
	 */
	public void write(File file, List<String> contents) throws IOException {
		if (contents != null) {
			for (String contentTemp : contents) {
				write(file, contentTemp + "\n");
			}
		}
	}

	/**
	 * Delete an existing file
	 * 
	 * @param file The file to be deleted
	 */
	public void delete(File file) throws IOException {
		Files.delete(file.toPath());
	}

	/**
	 * Delete the files of the input file list
	 * 
	 * @param files The list of files to be deleted
	 */
	public void delete(List<File> files) throws IOException {
		for (File fileToDelete : files) {
			delete(fileToDelete);
		}
	}

	public void writeCsv(List<NotificationCsvBean> notifications, OutputStream out)
			throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
		HeaderColumnNameMappingStrategy<NotificationCsvBean> strategy = new HeaderColumnNameMappingStrategy<>();
		strategy.setType(NotificationCsvBean.class);
		OutputStreamWriter writer = new OutputStreamWriter(out);
		StatefulBeanToCsv<NotificationCsvBean> csv = new StatefulBeanToCsvBuilder<NotificationCsvBean>(writer)
				.withMappingStrategy(strategy).build();
		csv.write(notifications);
	}

	/**
	 * Converts notification data into csv data object
	 * 
	 * @param notificationData the {@link NotificationData} object to convert
	 * @return A {@link NotificationCsvBean} object representing the converted input
	 *         object
	 */
	public NotificationCsvBean toCsv(NotificationData notificationData) {
		EscapeUtils escapeUtils = new EscapeUtils();
		NotificationCsvBean notification = new NotificationCsvBean();
		if (null != notificationData.getRecipients() && !notificationData.getRecipients().isEmpty()) {
			StringBuilder recipientsBuilder = new StringBuilder();
			for (String tempRecipient : notificationData.getRecipients()) {
				recipientsBuilder.append(tempRecipient + "-");
			}
			recipientsBuilder.deleteCharAt(recipientsBuilder.length() - 1);
			notification.setCodiciFiscali(escapeUtils.escapeForCsv(recipientsBuilder.toString()));
			recipientsBuilder.setLength(0);
		}
		notification.setIun(escapeUtils.escapeForCsv(notificationData.getIun()));
		notification.setDataInvio(escapeUtils.escapeForCsv(notificationData.getSentAt()));
		notification.setDataGenerazioneAttestazioneOpponibileATerzi(
				escapeUtils.escapeForCsv(notificationData.getRequestAcceptedAt()));
		notification.setOggetto(escapeUtils.escapeForCsv(notificationData.getSubject()));
		return notification;
	}

	/**
	 * Converts a list of notification data into csv data objects
	 * 
	 * @param notificationData the list of {@link NotificationData} objects to
	 *                         convert
	 * @return A list of {@link NotificationCsvBean} objects representing the
	 *         converted input objects
	 */
	public List<NotificationCsvBean> toCsv(List<NotificationData> notificationData) {
		ArrayList<NotificationCsvBean> csvNotifications = new ArrayList<>();
		for (NotificationData notification : notificationData) {
			csvNotifications.add(toCsv(notification));
		}
		return csvNotifications;
	}

	/**
	 * Write open search logs data to a txt file
	 * 
	 * @param openSearchLogs the list of {@link String} the contents from OpenSearch
	 *                       to write in the output file
	 * @param fileName       the name of file
	 * @return a new {@link File} instance of a file with the given name and content
	 */
	public File writeTxt(List<String> openSearchLogs, String fileName) throws IOException {
		File logFile = getFileWithRandomName(fileName, GenericConstants.TXT_EXTENSION);
		write(logFile, openSearchLogs);
		return logFile;
	}
}
