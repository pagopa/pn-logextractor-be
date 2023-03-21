package it.gov.pagopa.logextractor.util.external.opensearch;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import it.gov.pagopa.logextractor.dto.OpensearchScrollQueryData;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;
import it.gov.pagopa.logextractor.util.constant.OpensearchConstants;
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

import it.gov.pagopa.logextractor.util.RefactoryUtil;
import it.gov.pagopa.logextractor.util.SortOrders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Uility class for integrations with OpenSearch service
 * */
@Component
@Slf4j
public class OpenSearchApiHandler {
	@Autowired
	@Qualifier("openSearchRestTemplate")
	RestTemplate client;
	@Value("${external.opensearch.search.url}")
	String opensearchSearchUrl;
	@Value("${external.opensearch.search.followup.url}")
	String opensearchSearchFollowupUrl;
	@Value("${external.opensearch.basicauth.username}")
	String opensearchUsername;
	@Value("${external.opensearch.basicauth.password}")
	String opensearchPassword;
	
	/**
	 * Construct and executes a multi-search query searching for document
	 * with the given uid value within the input date range
	 * @param uid The uid to use for the multi-search query
	 * @param dateFrom The period start date
	 * @param dateTo The period end date
	 * @return The documents list contained into the Opensearch response
	 * */
	public List<String> getAnonymizedLogsByUid(String uid, LocalDate dateFrom, LocalDate dateTo){
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<>();
		HashMap<String, Object> queryParams = new HashMap<>();
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		log.info(LoggingConstants.QUERY_CONSTRUCTION);
		String queryUid = (StringUtils.startsWithIgnoreCase(uid, OpensearchConstants.UID_PF_PREFIX) ||
				StringUtils.startsWithIgnoreCase(uid, OpensearchConstants.UID_PG_PREFIX)) ?
				StringUtils.substring(uid, 3) : uid;
		queryParams.put(OpensearchConstants.OS_UID_FIELD, queryUid);
		queryData.add(queryConstructor.prepareQueryData(queryParams,
				new OpenSearchRangeQueryData(OpensearchConstants.OS_TIMESTAMP_FIELD, dateFrom.toString(), dateTo.toString()),
				new OpenSearchSortFilter(OpensearchConstants.OS_TIMESTAMP_FIELD, SortOrders.ASC)));
		String query = queryConstructor.createBooleanMultiSearchQuery(queryData);
		log.info(LoggingConstants.QUERY_EXECUTION + RegExUtils.removeAll(query, "\n"));
		//TODO: tornare qui
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		extractDocumentsFromOpensearch(query, baos);
		
		return RefactoryUtil.streamToList(baos);
	}
	
	/**
	 * Construct and executes a multi-search query searching for document
	 * with the given iun value within the input date range
	 * @param iun The iun to use for the multi-search query
	 * @param dateFrom The period start date
	 * @param dateTo The period end date
	 * @param out 
	 * @return The documents list contained into the Opensearch response
	 * */
	public int getAnonymizedLogsByIun(String iun, String dateFrom, String dateTo, OutputStream out) {
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<>();
		HashMap<String, Object> queryParams = new HashMap<>();
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		log.info(LoggingConstants.QUERY_CONSTRUCTION);
		queryParams.put(OpensearchConstants.OS_IUN_FIELD, iun);
		queryData.add(queryConstructor.prepareQueryData(queryParams,
				new OpenSearchRangeQueryData(OpensearchConstants.OS_TIMESTAMP_FIELD, dateFrom, dateTo),
				new OpenSearchSortFilter(OpensearchConstants.OS_TIMESTAMP_FIELD, SortOrders.ASC)));
		String query = queryConstructor.createBooleanMultiSearchQuery(queryData);
		log.info(LoggingConstants.QUERY_EXECUTION + RegExUtils.removeAll(query, "\n"));
		return extractDocumentsFromOpensearch(query, out);
	}
	
	/**
	 * Construct and executes a multi-search query searching for document with
	 * the given trace id value within the input date range
	 * @param traceId The trace id to use for the multi-search query
	 * @param dateFrom The period start date
	 * @param dateTo The period end date
	 * @return The documents list contained into the Opensearch response
	 * */
	public List<String> getAnonymizedLogsByTraceId(String traceId, LocalDate dateFrom, LocalDate dateTo){
		HashMap<String, Object> queryParams = new HashMap<>();
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		log.info(LoggingConstants.QUERY_CONSTRUCTION);
		queryParams.put(OpensearchConstants.OS_TRACE_ID_FIELD, traceId);
		OpenSearchQuerydata queryData = queryConstructor.prepareQueryData(queryParams,
				new OpenSearchRangeQueryData(OpensearchConstants.OS_TIMESTAMP_FIELD, dateFrom.toString(), dateTo.toString()),
				new OpenSearchSortFilter(OpensearchConstants.OS_TIMESTAMP_FIELD, SortOrders.ASC));
		ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
		listOfQueryData.add(queryData);
		String query = queryConstructor.createBooleanMultiSearchQuery(listOfQueryData);
		log.info(LoggingConstants.QUERY_EXECUTION + RegExUtils.removeAll(query, "\n"));

		//TODO: rivedere
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		extractDocumentsFromOpensearch(query, baos);
		
		return RefactoryUtil.streamToList(baos);
	}
	
	/**
	 * Construct and executes a multi-search query searching for document
	 * with the given jti value within the input date range
	 * @param jti The jti value to use for the multi-search query
	 * @param dateFrom The period start date
	 * @param dateTo The period end date
	 * @return The documents list contained into the Opensearch response
	 * */
	public List<String> getAnonymizedSessionLogsByJti(String jti, LocalDate dateFrom, LocalDate dateTo){
		HashMap<String, Object> queryParams = new HashMap<>();
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		log.info(LoggingConstants.QUERY_CONSTRUCTION);
		queryParams.put(OpensearchConstants.OS_JTI_FIELD, jti);
		OpenSearchQuerydata queryData = queryConstructor.prepareQueryData(queryParams,
				new OpenSearchRangeQueryData(OpensearchConstants.OS_TIMESTAMP_FIELD, dateFrom.toString(), dateTo.toString()),
				new OpenSearchSortFilter(OpensearchConstants.OS_TIMESTAMP_FIELD, SortOrders.ASC));
		ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
		listOfQueryData.add(queryData);
		String query = queryConstructor.createBooleanMultiSearchQuery(listOfQueryData);
		log.info(LoggingConstants.QUERY_EXECUTION + RegExUtils.removeAll(query, "\n"));

		
		//TODO: temporary code to keep old logic working
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		extractDocumentsFromOpensearch(query, baos);
		
		return RefactoryUtil.streamToList(baos);
	}
	
	
	private HttpHeaders buildHeaders() {
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setBasicAuth(opensearchUsername, opensearchPassword);
        List<MediaType> acceptedTypes = new ArrayList<>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        return requestHeaders;
	}
	
	/**
	 * Performs a search HTTP GET request to the Opensearch service and extract the documents
	 * that satisfy the input query
	 * @param query The search query to be sent
	 * @param out 
	 * @return The number of documents contained into the Opensearch response
	 * */
	private int extractDocumentsFromOpensearch(String query, OutputStream out) {
		HttpHeaders requestHeaders = buildHeaders();
        HttpEntity<String> request = new HttpEntity<>(query, requestHeaders);
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(opensearchSearchUrl)
						.queryParam(OpensearchConstants.OS_SCROLL_PARAMETER, "{scroll}")
						.encode()
						.toUriString();
		HashMap<String, Object> params = new HashMap<>();
		params.put(OpensearchConstants.OS_SCROLL_PARAMETER, OpensearchConstants.OS_SCROLL_ID_VALIDITY_DURATION);
        String response = client.exchange(
				urlTemplate,
				HttpMethod.GET,
				request,
				String.class,
				params).getBody();
		//return getDocumentsFromOpensearchResponse(response, new ArrayList<>());
        
        int counter = 0;
        ArrayList<String> currentDocs;
        try {
	        while (!( currentDocs = getDocumentsFromCurrentResponse(response)).isEmpty()){
	        	for(String line : currentDocs) {
	        		out.write(line.getBytes(StandardCharsets.UTF_8));
	        		counter ++;
	        	}
	        	OpensearchScrollQueryData scrollQueryDto = new OpensearchScrollQueryData(
	        			OpensearchConstants.OS_SCROLL_ID_VALIDITY_DURATION,
	        			new JSONObject(response).getString(OpensearchConstants.OS_RESPONSE_SCROLL_ID_FIELD));
	        	HttpEntity<OpensearchScrollQueryData> requestScroll = new HttpEntity<>(scrollQueryDto, requestHeaders);
	        	response = client.exchange(opensearchSearchFollowupUrl,HttpMethod.GET,requestScroll,String.class).getBody();
	        }
        } catch (IOException e) {
        	log.error("Error writing OpenSearch logs to stream", e);
        	counter = -1;
        }
        return counter;
	}

	/**
	 * Recursively performs scroll HTTP GET requests to Opensearch service to get the document list page util
	 * all the documents have been retrieved
	 * @param openSearchResponse The opensearch response to get the documents from
	 * @param documents The document list to be returned
	 * @return The documents list after all the scroll iterations into the Opensearch response
	 * */
	private ArrayList<String> getDocumentsFromOpensearchResponse(String openSearchResponse, ArrayList<String> documents){
		ArrayList<String> currentDocs = getDocumentsFromCurrentResponse(openSearchResponse);
		if(currentDocs.isEmpty()){
			return documents;
		}
		documents.addAll(currentDocs);
		HttpHeaders requestHeaders = buildHeaders();
		OpensearchScrollQueryData scrollQueryDto = new OpensearchScrollQueryData(
				OpensearchConstants.OS_SCROLL_ID_VALIDITY_DURATION,
				new JSONObject(openSearchResponse).getString(OpensearchConstants.OS_RESPONSE_SCROLL_ID_FIELD));
		HttpEntity<OpensearchScrollQueryData> request = new HttpEntity<>(scrollQueryDto, requestHeaders);
		ResponseEntity<String> response = client.exchange(opensearchSearchFollowupUrl,
				HttpMethod.GET,request,String.class);
		return getDocumentsFromOpensearchResponse(response.getBody(), documents);
	}
	
	/**
	 * Gets the document list from an Opensearch response page
	 * @param openSearchResponseBody The current Opensearch response
	 * @return The decouments list
	 * */
	private ArrayList<String> getDocumentsFromCurrentResponse(String openSearchResponseBody) {
		ArrayList<String> documents = new ArrayList<>();
		if (!StringUtils.isEmpty(openSearchResponseBody)) {
			JSONObject documentListObject = new JSONObject(openSearchResponseBody);
			if (!documentListObject.isNull("hits")) {
				JSONObject documentData = documentListObject.getJSONObject("hits");
				if (!documentData.isNull("hits")) {
					JSONArray opensearchEnrichedDoc = documentData.getJSONArray("hits");
					for (int hitIndex = 0; hitIndex < opensearchEnrichedDoc.length(); hitIndex++) {
						if (!opensearchEnrichedDoc.getJSONObject(hitIndex).isNull("_source")) {
							documents.add(
									opensearchEnrichedDoc.getJSONObject(hitIndex).getJSONObject("_source").toString());
						}
					}
				}
			}
		}
		return documents;
	}
}
