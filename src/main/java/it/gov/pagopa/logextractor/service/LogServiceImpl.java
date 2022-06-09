package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.NotificationGeneralData;
import it.gov.pagopa.logextractor.dto.request.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.PasswordFactory;
import it.gov.pagopa.logextractor.util.ZipFactory;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchQueryConstructor;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchQueryFilter;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchQuerydata;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchRangeQueryData;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;

@Service
public class LogServiceImpl implements LogService{
	
	@Value("${external.opensearch.url}")
	String openSearchURL;
	
	@Value("${external.opensearch.basicauth.username}")
	String openSearchUsername;
	
	@Value("${external.opensearch.basicauth.password}")
	String openSearchPassword;
	
	@Value("${external.notification.getSentNotification.url}")
	String notificationURL;

	@Override
	public DownloadArchiveResponseDto getPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun, String personId) throws IOException {
		
		ArrayList<String> openSearchResponse = null;
		
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && iun == null) {
			openSearchResponse = getDocumentsFromOpenSearch(constructQuery("logs-1", "internalid", personId, new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo)));
		} else {
			// use case 8
			if (iun != null) {
								
				NotificationApiHandler notificationHandler = new NotificationApiHandler();
				
				String legalStartDate = notificationHandler.getNotificationLegalStartDate(notificationURL, iun);
				
				String dateIn3Months = OffsetDateTime.parse(legalStartDate).plusMonths(3).toString();
				System.out.println(legalStartDate);
				System.out.println(dateIn3Months);

				openSearchResponse = getDocumentsFromOpenSearch(constructQuery("logs-2", "internalid", iun, new OpenSearchRangeQueryData("@timestamp", legalStartDate, dateIn3Months)));	
			}
		}
		
		PasswordFactory passwordFactory = new PasswordFactory();
		String password = passwordFactory.createPassword(1, 1, 1, Constants.PASSWORD_SPECIAL_CHARS, 1, 16);
		FileUtilities utils = new FileUtilities();
		File file = utils.getFile(Constants.FILE_NAME,Constants.TXT_EXTENSION);
		utils.write(file, openSearchResponse);
		ZipFactory zipFactory = new ZipFactory();
		ZipFile zipArchive = zipFactory.createZipArchive(Constants.ZIP_ARCHIVE_NAME, password);
		ZipParameters params = zipFactory.createZipParameters(true, CompressionLevel.HIGHER, EncryptionMethod.AES);
		zipArchive = zipFactory.addFile(zipArchive, params, file);
		byte[] zipfile = zipFactory.toByteArray(zipArchive);
		utils.deleteFile(file);
		utils.deleteFile(FileUtils.getFile(zipArchive.toString()));
		return DownloadArchiveResponseDto.builder().password(password).zip(zipfile).build();
	}


	@Override
	public DownloadArchiveResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode) throws IOException, ParseException,CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		LocalDate startDate = LocalDate.parse(StringUtils.removeIgnoreCase(referenceMonth, "-")+"01", DateTimeFormatter.BASIC_ISO_DATE);
		LocalDate endDate = startDate.plusMonths(1);
		NotificationApiHandler notificationApiHandler = new NotificationApiHandler();
		ArrayList<NotificationGeneralData> notificationsGeneralData = notificationApiHandler.getNotificationsByPeriod(ipaCode, 
																		startDate.toString(), endDate.toString(), 10);
		return null;
	}
	
	/**
	 * Method that constructs a boolean multisearch query
	 * @param indexName the name of the index in OpenSearch
	 * @param searchField the field by which the search will be performed
	 * @param searchValue the value of the searched field
	 * @param rangeQueryData date range, if it is any
	 * @return the constructed query, ready to be passed to OpenSearch
	 */
	private String constructQuery(String indexName, String searchField, String searchValue, OpenSearchRangeQueryData rangeQueryData) {
		OpenSearchQueryFilter internalIdFilter = new OpenSearchQueryFilter(searchField, searchValue);
		ArrayList<OpenSearchQueryFilter> simpleQueryFilters = new ArrayList<>();
		simpleQueryFilters.add(internalIdFilter);
		OpenSearchQuerydata simpleQueryData = new OpenSearchQuerydata(indexName, simpleQueryFilters, rangeQueryData);
		ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
		listOfQueryData.add(simpleQueryData);
		return new OpenSearchQueryConstructor().createBooleanMultiSearchQuery(listOfQueryData);
	}
	
	/**
	 * Method that sends a request to OpenSearch, performing multisearch with a given query
	 * @param query boolean query that will be used in order to perform multisearch
	 * @return documents that will be returned from OpenSearch
	 */
	private ArrayList<String> getDocumentsFromOpenSearch(String query) {
		OpenSearchApiHandler openSearchHandler = new OpenSearchApiHandler();
		return openSearchHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
	}
}
