package it.gov.pagopa.logextractor.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.gov.pagopa.logextractor.dto.response.DownloadLogResponseDto;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchQueryConstructor;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchQueryFilter;
import it.gov.pagopa.logextractor.util.opensearch.OpenSearchQuerydata;

@Service
public class LogServiceImpl implements LogService{
	
	@Value("${external.opensearch.url}")
	String openSearchURL;
	
	@Value("${external.opensearch.basicauth.username}")
	String openSearchUsername;
	
	@Value("${external.opensearch.basicauth.password}")
	String openSearchPassword;

	@Override
	public DownloadLogResponseDto getPersonLogs(String dateFrom, String dateTo, String referenceDate, String ticketNumber, Integer uin, String personId) {
		
		OpenSearchApiHandler openSearchHandler = new OpenSearchApiHandler();
		ArrayList<String> openSearchResponse = null;
		
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && uin == null) {
			OpenSearchQueryFilter simpleFilter = new OpenSearchQueryFilter("internalid", personId);
			ArrayList<OpenSearchQueryFilter> simpleQueryFilters = new ArrayList<>();
			simpleQueryFilters.add(simpleFilter);
			OpenSearchQuerydata simpleQueryData = new OpenSearchQuerydata("logs-1", simpleQueryFilters);
			ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
			listOfQueryData.add(simpleQueryData);
			String query = new OpenSearchQueryConstructor().createMultiSearchQuery(listOfQueryData, false);
			openSearchResponse = openSearchHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		} else {
			// use case 8
			if (uin != null) {
				OpenSearchQueryFilter simpleFilter = new OpenSearchQueryFilter("internalid", uin.toString());
				ArrayList<OpenSearchQueryFilter> simpleQueryFilters = new ArrayList<>();
				simpleQueryFilters.add(simpleFilter);
				OpenSearchQuerydata simpleQueryData = new OpenSearchQuerydata("logs-2", simpleQueryFilters);
				ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
				listOfQueryData.add(simpleQueryData);
				String query = new OpenSearchQueryConstructor().createMultiSearchQuery(listOfQueryData, false);
				openSearchResponse = openSearchHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			}
		}
		
		return null;
	}


}
