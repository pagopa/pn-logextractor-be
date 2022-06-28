package it.gov.pagopa.logextractor.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.NotificationCsvBean;
import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public class ResponseConstructor {

	/**
	 * Manages the response creation phase.
	 * 
	 * @param contents the contents to write in the output file (.txt) contained in
	 *                 the output zip archive
	 * @param fileName the name of the output file contained in the output zip
	 *                 archive
	 * @param zipName  the name of the output zip archive
	 * @throws IOException in case IO errors
	 * @return DownloadArchiveResponseDto A Dto containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 */
	public static DownloadArchiveResponseDto createSimpleLogResponse(ArrayList<String> contents, String fileName, String zipName) throws IOException {
		PasswordFactory passwordFactory = new PasswordFactory();
		String password = passwordFactory.createPassword(1, 1, 1, Constants.PASSWORD_SPECIAL_CHARS, 1, 16);
		FileUtilities utils = new FileUtilities();
		File file = utils.getFile(fileName,Constants.TXT_EXTENSION);
		utils.write(file, contents);
		ZipFactory zipFactory = new ZipFactory();
		ZipFile zipArchive = zipFactory.createZipArchive(zipName, password);
		ZipParameters params = zipFactory.createZipParameters(true, CompressionLevel.HIGHER, EncryptionMethod.AES);
		zipArchive = zipFactory.addFile(zipArchive, params, file);
		byte[] zipfile = zipFactory.toByteArray(zipArchive);
		utils.deleteFile(file);
		utils.deleteFile(FileUtils.getFile(zipArchive.toString()));
		return DownloadArchiveResponseDto.builder().password(password).zip(zipfile).build();
	}
	
	/**
	 * Manages the response creation phase.
	 * 
	 * @param contents the contents to write in the output file (.csv) contained in
	 *                 the output zip archive
	 * @param fileName the name of the output file contained in the output zip
	 *                 archive
	 * @param zipName  the name of the output zip archive
	 * @throws IOException in case IO errors
	 * @return DownloadArchiveResponseDto A Dto containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws CsvRequiredFieldEmptyException
	 * @throws CsvDataTypeMismatchException
	 */
	public static DownloadArchiveResponseDto createCsvLogResponse(ArrayList<NotificationCsvBean> notifications, String fileName, String zipName) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		PasswordFactory passwordFactory = new PasswordFactory();
		String password = passwordFactory.createPassword(1, 1, 1, Constants.PASSWORD_SPECIAL_CHARS, 1, 16);
		FileUtilities utils = new FileUtilities();
		File file = utils.getFile(fileName,Constants.CSV_EXTENSION);
		utils.writeCsv(file, notifications);
		ZipFactory zipFactory = new ZipFactory();
		ZipFile zipArchive = zipFactory.createZipArchive(zipName, password);
		ZipParameters params = zipFactory.createZipParameters(true, CompressionLevel.HIGHER, EncryptionMethod.AES);
		zipArchive = zipFactory.addFile(zipArchive, params, file);
		byte[] zipfile = zipFactory.toByteArray(zipArchive);
		utils.deleteFile(file);
		utils.deleteFile(FileUtils.getFile(zipArchive.toString()));
		return DownloadArchiveResponseDto.builder().password(password).zip(zipfile).build();
	}
	
	/**
	 * Method that manages the notification logs response creation phase.
	 * 
	 * @param openSearchLogs the contents from OpenSearch to write in the output
	 *                       file (.txt), contained in the output zip archive
	 * @param filesToAdd     list, containing every notification file to add in the
	 *                       zip archive
	 * @param fileName       the name of file, containing the logs in the output zip
	 *                       archive
	 * @param zipName        the name of the output zip archive
	 * @return {@link DownloadArchiveResponseDto} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException
	 */
	public static DownloadArchiveResponseDto createNotificationLogResponse(ArrayList<String> openSearchLogs, ArrayList<File> filesToAdd, String fileName, String zipName) throws IOException {
		PasswordFactory passwordFactory = new PasswordFactory();
		String password = passwordFactory.createPassword(1, 1, 1, Constants.PASSWORD_SPECIAL_CHARS, 1, 16);
		FileUtilities utils = new FileUtilities();
		File file = utils.getFile(fileName, Constants.TXT_EXTENSION);
		utils.write(file, openSearchLogs);
		ZipFactory zipFactory = new ZipFactory();
		ZipFile zipArchive = zipFactory.createZipArchive(zipName, password);
		ZipParameters params = zipFactory.createZipParameters(true, CompressionLevel.HIGHER, EncryptionMethod.AES);
		zipArchive = zipFactory.addFile(zipArchive, params, file);
		for (File fileToAdd : filesToAdd) {
			zipArchive = zipFactory.addFile(zipArchive, params, fileToAdd);
		}
		byte[] zipfile = zipFactory.toByteArray(zipArchive);
		for(File fileToDelete : filesToAdd) {
			utils.deleteFile(fileToDelete);
		}
		utils.deleteFile(file);
		utils.deleteFile(FileUtils.getFile(zipArchive.toString()));
		return DownloadArchiveResponseDto.builder().password(password).zip(zipfile).build();
	}
}
