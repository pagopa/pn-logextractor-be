package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.NotificationGeneralData;
import it.gov.pagopa.logextractor.dto.response.PasswordResponseDto;
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

	@Override
	public byte[] getPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun, String personId, String password) throws IOException {
		
		OpenSearchApiHandler openSearchHandler = new OpenSearchApiHandler();
		ArrayList<String> openSearchResponse = null;
		
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && iun == null) {
			System.out.println("use case 7");
			OpenSearchQueryFilter internalIdFilter = new OpenSearchQueryFilter("internalid", personId);
			ArrayList<OpenSearchQueryFilter> simpleQueryFilters = new ArrayList<>();
			simpleQueryFilters.add(internalIdFilter);
			OpenSearchQuerydata simpleQueryData = new OpenSearchQuerydata("logs-1", simpleQueryFilters, new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo));
			ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
			listOfQueryData.add(simpleQueryData);
			String query = new OpenSearchQueryConstructor().createBooleanMultiSearchQuery(listOfQueryData);
			System.out.println("Query:\n" + query);
			openSearchResponse = openSearchHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		} else {
			// use case 8
			if (iun != null) {
				System.out.println("use case 8");
				OpenSearchQueryFilter internalIdFilter = new OpenSearchQueryFilter("internalid", iun.toString());
				ArrayList<OpenSearchQueryFilter> queryFilters = new ArrayList<>();
				queryFilters.add(internalIdFilter);
				OpenSearchQuerydata queryData = new OpenSearchQuerydata("logs-2", queryFilters, null);
				ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
				listOfQueryData.add(queryData);
				
				//TODO: adjust the query with boolean range query with notification create date (start date) until maximum 3 months forward 
				String query = new OpenSearchQueryConstructor().createBooleanMultiSearchQuery(listOfQueryData);
				openSearchResponse = openSearchHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			}
		}
		
		FileUtilities utils = new FileUtilities();
		File file = utils.getFile(Constants.FILE_NAME,Constants.TXT_EXTENSION);
		utils.write(file, openSearchResponse);
		ZipFactory zipFactory = new ZipFactory();
		var zipArchive = zipFactory.createZipArchive(Constants.ZIP_ARCHIVE_NAME, password);
		ZipParameters params = zipFactory.createZipParameters(true, CompressionLevel.HIGHER, EncryptionMethod.AES);
		zipArchive = zipFactory.addFile(zipArchive, params, file);
		byte[] response = zipFactory.toByteArray(zipArchive);
		utils.deleteFile(file);
		utils.deleteFile(FileUtils.getFile(zipArchive.toString()));
		return response;
	}


	@Override
	public byte[] getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode, String password) throws IOException, ParseException,CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		LocalDate startDate = LocalDate.parse(StringUtils.removeIgnoreCase(referenceMonth, "-")+"01", DateTimeFormatter.BASIC_ISO_DATE);
		LocalDate endDate = startDate.plusMonths(1);
		NotificationApiHandler notificationApiHandler = new NotificationApiHandler();
		ArrayList<NotificationGeneralData> notificationsGeneralData = notificationApiHandler.getNotificationsByPeriod(ipaCode, 
																		startDate.toString(), endDate.toString(), 10);
		return null;
	}

	@Override
	public PasswordResponseDto createPassword() {
		return PasswordResponseDto.builder().password(new PasswordFactory().createPassword(1, 1, 1, Constants.PASSWORD_SPECIAL_CHARS, 1, 16)).build();
	}
}
