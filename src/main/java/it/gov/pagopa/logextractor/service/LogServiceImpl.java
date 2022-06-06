package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.NotificationCsvBean;
import it.gov.pagopa.logextractor.dto.response.PasswordResponseDto;
import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.PasswordFactory;
import it.gov.pagopa.logextractor.util.ZipFactory;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchQueryConstructor;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchQueryFilter;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchQuerydata;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchRangeQueryData;
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

//	@Override
	public PasswordResponseDto getPersonLogs(String dateFrom, String dateTo, String referenceDate, String ticketNumber, Integer uin, String personId) {
		
		OpenSearchApiHandler openSearchHandler = new OpenSearchApiHandler();
		ArrayList<String> openSearchResponse = null;
		
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && uin == null) {
			OpenSearchQueryFilter simpleFilter = new OpenSearchQueryFilter("internalid", personId);
			ArrayList<OpenSearchQueryFilter> simpleQueryFilters = new ArrayList<>();
			simpleQueryFilters.add(simpleFilter);
			OpenSearchQuerydata simpleQueryData = new OpenSearchQuerydata("logs-1", simpleQueryFilters, null);
			List<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
			listOfQueryData.add(simpleQueryData);
//			String query = new OpenSearchQueryConstructor().createMultiSearchQuery(listOfQueryData);
//			openSearchResponse = openSearchHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		} else {
			// use case 8
			if (uin != null) {
				OpenSearchQueryFilter simpleFilter = new OpenSearchQueryFilter("internalid", uin.toString());
				ArrayList<OpenSearchQueryFilter> simpleQueryFilters = new ArrayList<>();
				simpleQueryFilters.add(simpleFilter);
				OpenSearchQuerydata simpleQueryData = new OpenSearchQuerydata("logs-2", simpleQueryFilters, null);
				ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
				listOfQueryData.add(simpleQueryData);
//				String query = new OpenSearchQueryConstructor().createMultiSearchQuery(listOfQueryData);
//				openSearchResponse = openSearchHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			}
		}
		
		return null;
	}

	@Override
	public PasswordResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode) throws IOException, ParseException,CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		return null;
	}

	@Override
	public PasswordResponseDto createPassword() {
		return PasswordResponseDto.builder().password(new PasswordFactory().createPassword(2, 2, 2, Constants.PASSWORD_SPECIAL_CHARS, 2, 16)).build();
	}
	
	
}
