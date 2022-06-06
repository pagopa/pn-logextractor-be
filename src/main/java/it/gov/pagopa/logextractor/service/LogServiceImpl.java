package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.response.PasswordResponseDto;
import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.PasswordFactory;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchQueryConstructor;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchQueryFilter;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchQuerydata;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchRangeQueryData;

@Service
public class LogServiceImpl implements LogService{
	
	@Value("${external.opensearch.url}")
	String openSearchURL;
	
	@Value("${external.opensearch.basicauth.username}")
	String openSearchUsername;
	
	@Value("${external.opensearch.basicauth.password}")
	String openSearchPassword;

//	@Override
	public PasswordResponseDto getPersonLogs(String dateFrom, String dateTo, String referenceDate, String ticketNumber, Integer uin, String personId, String password) {
		
		OpenSearchApiHandler openSearchHandler = new OpenSearchApiHandler();
		ArrayList<String> openSearchResponse = null;
		
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && uin == null) {
			System.out.println("use case 7");
			OpenSearchQueryFilter internalIdFilter = new OpenSearchQueryFilter("internalid", personId);
			ArrayList<OpenSearchQueryFilter> simpleQueryFilters = new ArrayList<>();
			simpleQueryFilters.add(internalIdFilter);
			OpenSearchQuerydata simpleQueryData = new OpenSearchQuerydata("logs-1", simpleQueryFilters, new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo));
			ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
			listOfQueryData.add(simpleQueryData);
			String query = new OpenSearchQueryConstructor().createSimpleMultiSearchQuery(listOfQueryData);
			openSearchResponse = openSearchHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		} else {
			// use case 8
			if (uin != null) {
				System.out.println("use case 8");
				OpenSearchQueryFilter internalIdFilter = new OpenSearchQueryFilter("internalid", uin.toString());
				ArrayList<OpenSearchQueryFilter> queryFilters = new ArrayList<>();
				queryFilters.add(internalIdFilter);
				if (referenceDate != null) {
					OpenSearchQueryFilter timestampFilter = new OpenSearchQueryFilter("@timestamp", referenceDate);
					queryFilters.add(timestampFilter);
				}
				OpenSearchQuerydata queryData = new OpenSearchQuerydata("logs-2", queryFilters, null);
				ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
				listOfQueryData.add(queryData);
				
				String query;
				
				if (referenceDate != null) {					
					query = new OpenSearchQueryConstructor().createBooleanMultiSearchQuery(listOfQueryData);
				} else {
					query = new OpenSearchQueryConstructor().createSimpleMultiSearchQuery(listOfQueryData);
				}
				openSearchResponse = openSearchHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			}
		}
		
//				FileUtilities utils = new FileUtilities();
//				File file = utils.getFile("C:\\Users\\msarkisian\\OneDrive - DXC Production\\Documents\\LogExtractor\\Files\\personLogs.txt");
//				utils.write(file, openSearchResponse);
//				
//				System.out.println(password);
//				var zipFile = ZipFactory.createZipArchive("C:\\Users\\msarkisian\\OneDrive - DXC Production\\Documents\\LogExtractor\\Files\\archive.zip", password);
//				System.out.println(zipFile.getFile().getName());
//				zipFile.addFile(file);
		
		return PasswordResponseDto.builder().password(password).build();
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
