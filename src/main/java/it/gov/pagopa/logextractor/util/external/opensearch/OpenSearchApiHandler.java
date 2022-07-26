package it.gov.pagopa.logextractor.util.external.opensearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	public ArrayList<String> getAnonymizedLogsByUid(String uid, String dateFrom, String dateTo){
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<OpenSearchQuerydata>();
		HashMap<String, Object> queryParams = new HashMap<String, Object>();
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		log.info("Constructing Opensearch query...");
//		queryParams.put("uid.keyword", StringUtils.substring(uid, 3));
		queryParams.put("uid", StringUtils.substring(uid, 3));
		queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
				new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
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
	public ArrayList<String> getAnonymizedLogsByIun(String iun, String dateFrom, String dateTo) {
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<OpenSearchQuerydata>();
		HashMap<String, Object> queryParams = new HashMap<String, Object>();
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		log.info("Constructing Opensearch query...");
//		queryParams.put("iun.keyword", iun);
		queryParams.put("iun", iun);
		queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
				new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
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
	public ArrayList<String> getAnonymizedLogsByTraceId(String traceId, String dateFrom, String dateTo){
		HashMap<String, Object> queryParams = new HashMap<String, Object>();
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		log.info("Constructing Opensearch query...");
//		queryParams.put("root_trace_id.keyword", traceId);
		queryParams.put("root_trace_id", traceId);
		OpenSearchQuerydata queryData = queryConstructor.prepareQueryData("pn-logs", queryParams, 
				new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC));
		ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
		listOfQueryData.add(queryData);
		String query = queryConstructor.createBooleanMultiSearchQuery(listOfQueryData);
		log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
		return getDocumentsByMultiSearchQuery(query);
	}
	
	/**
	 * Performs a multi-search HTTP GET request to the Opensearch service
	 * @param query The mmulti-search query to the sent
	 * @param basicAuthUsername The username for the basic authentication
	 * @param basicAuthPassword The password for the basic authentication
	 * @return The documents list contained into the Opensearch response
	 * */
	private ArrayList<String> getDocumentsByMultiSearchQuery(String query) {
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setBasicAuth(openSearchUsername, openSearchPassword);
        List<MediaType> acceptedTypes = new ArrayList<MediaType>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HttpEntity<String> request = new HttpEntity<String>(query, requestHeaders);
        ResponseEntity<String> response = client.exchange(openSearchHost+"/_msearch", HttpMethod.GET, request, String.class);
        return getDocuments(response.getBody());
	}
	
	/**
	 * Gets the document list from the Opensearch response
	 * @param openSearchResponseBody The Opensearch response
	 * @return The decouments list
	 * */
	private ArrayList<String> getDocuments(String openSearchResponseBody) {
		ArrayList<String> documents = new ArrayList<String>();
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
