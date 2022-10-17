package it.gov.pagopa.logextractor.util.external.opensearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.gov.pagopa.logextractor.util.Constants;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.gov.pagopa.logextractor.util.SortOrders;
import lombok.extern.slf4j.Slf4j;

/**
 * Uility class for integrations with OpenSearch service
 * */
@Component
@Slf4j
public class OpenSearchApiHandler {
	
	@Autowired
	@Qualifier("openSearchRestTemplate")
	RestTemplate client;
	
	@Value("${external.opensearch.host}")
	String openSearchHost;
	
	@Value("${external.opensearch.basicauth.username}")
	String openSearchUsername;
	
	@Value("${external.opensearch.basicauth.password}")
	String openSearchPassword;

	
	/**
	 * Construct and executes a multi-search query searching for document with the given uid value within the input date range
	 * @param uid The uid to use for the multi-search query
	 * @param dateFrom The period start date
	 * @param dateTo The period end date
	 * @return The documents list contained into the Opensearch response
	 * */
	public List<String> getAnonymizedLogsByUid(String uid, String dateFrom, String dateTo){
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<>();
		HashMap<String, Object> queryParams = new HashMap<>();
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		log.info("Constructing Opensearch query...");
//		queryParams.put("uid.keyword", StringUtils.substring(uid, 3));
		String queryUid = StringUtils.startsWithIgnoreCase(uid, Constants.UID_APIKEY_PREFIX) ?
				StringUtils.substring(uid, 7) : StringUtils.substring(uid, 3);
		queryParams.put(Constants.OS_UID_FIELD, queryUid);
		queryData.add(queryConstructor.prepareQueryData(Constants.QUERY_INDEX_ALIAS, queryParams,
				new OpenSearchRangeQueryData(Constants.OS_TIMESTAMP_FIELD, dateFrom, dateTo),
				new OpenSearchSortFilter(Constants.OS_TIMESTAMP_FIELD, SortOrders.ASC)));
		String query = queryConstructor.createBooleanMultiSearchQuery(queryData);
		log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
		return getDocumentsByMultiSearchQuery(query);
	}
	
	/**
	 * Construct and executes a multi-search query searching for document with the given iun value within the input date range
	 * @param iun The iun to use for the multi-search query
	 * @param dateFrom The period start date
	 * @param dateTo The period end date
	 * @return The documents list contained into the Opensearch response
	 * */
	public List<String> getAnonymizedLogsByIun(String iun, String dateFrom, String dateTo) {
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<>();
		HashMap<String, Object> queryParams = new HashMap<>();
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		log.info("Constructing Opensearch query...");
//		queryParams.put("iun.keyword", iun);
		queryParams.put(Constants.OS_IUN_FIELD, iun);
		queryData.add(queryConstructor.prepareQueryData(Constants.QUERY_INDEX_ALIAS, queryParams,
				new OpenSearchRangeQueryData(Constants.OS_TIMESTAMP_FIELD, dateFrom, dateTo), 
				new OpenSearchSortFilter(Constants.OS_TIMESTAMP_FIELD, SortOrders.ASC)));
		String query = queryConstructor.createBooleanMultiSearchQuery(queryData);
		log.info("Executing query:" + RegExUtils.removeAll(query, "\n"));
		return getDocumentsByMultiSearchQuery(query);
	}
	
	/**
	 * Construct and executes a multi-search query searching for document with the given trace id value within the input date range
	 * @param traceId The trace id to use for the multi-search query
	 * @param dateFrom The period start date
	 * @param dateTo The period end date
	 * @return The documents list contained into the Opensearch response
	 * */
	public List<String> getAnonymizedLogsByTraceId(String traceId, String dateFrom, String dateTo){
		HashMap<String, Object> queryParams = new HashMap<>();
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		log.info("Constructing Opensearch query...");
//		queryParams.put("root_trace_id.keyword", traceId);
		queryParams.put(Constants.OS_TRACE_ID_FIELD, traceId);
		OpenSearchQuerydata queryData = queryConstructor.prepareQueryData(Constants.QUERY_INDEX_ALIAS, queryParams,
				new OpenSearchRangeQueryData(Constants.OS_TIMESTAMP_FIELD, dateFrom, dateTo), 
				new OpenSearchSortFilter(Constants.OS_TIMESTAMP_FIELD, SortOrders.ASC));
		ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
		listOfQueryData.add(queryData);
		String query = queryConstructor.createBooleanMultiSearchQuery(listOfQueryData);
		log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
		return getDocumentsByMultiSearchQuery(query);
	}
	
	/**
	 * Performs a multi-search HTTP GET request to the Opensearch service
	 * @param query The multi-search query to the sent
	 * @return The documents list contained into the Opensearch response
	 * */
	private ArrayList<String> getDocumentsByMultiSearchQuery(String query) {
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setBasicAuth(openSearchUsername, openSearchPassword);
        List<MediaType> acceptedTypes = new ArrayList<>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HttpEntity<String> request = new HttpEntity<>(query, requestHeaders);
        ResponseEntity<String> response = client.exchange(openSearchHost+Constants.OS_MULTI_SEARCH_SUFFIX, HttpMethod.GET, request, String.class);
        return getDocuments(response.getBody());
	}
	
	/**
	 * Gets the document list from the Opensearch response
	 * @param openSearchResponseBody The Opensearch response
	 * @return The decouments list
	 * */
	private ArrayList<String> getDocuments(String openSearchResponseBody) {
		ArrayList<String> documents = new ArrayList<>();
		JSONObject json = new JSONObject(openSearchResponseBody);
		if(!json.isNull("responses")) {
			JSONArray responsesObject = new JSONObject(openSearchResponseBody).getJSONArray("responses");
	        for(int index = 0; index < responsesObject.length(); index++) {
	        	if(!responsesObject.getJSONObject(index).isNull("hits")) {
		        	JSONObject obj = responsesObject.getJSONObject(index).getJSONObject("hits");
		        	if(!obj.isNull("hits")) {
			        	JSONArray opensearchEnrichedDoc = obj.getJSONArray("hits");
			        	for(int hitIndex = 0; hitIndex < opensearchEnrichedDoc.length(); hitIndex++) {
			        		if(!opensearchEnrichedDoc.getJSONObject(hitIndex).isNull("_source")) {
			        			documents.add(opensearchEnrichedDoc.getJSONObject(hitIndex).getJSONObject("_source").toString());
			        		}
			        	}
		        	}
	        	}
	        }
		}
        return documents;
	}
}
