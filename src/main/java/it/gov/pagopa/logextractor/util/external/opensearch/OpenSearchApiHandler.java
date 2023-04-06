package it.gov.pagopa.logextractor.util.external.opensearch;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import it.gov.pagopa.logextractor.dto.OpensearchScrollQueryData;
import it.gov.pagopa.logextractor.util.SortOrders;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;
import it.gov.pagopa.logextractor.util.constant.OpensearchConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Uility class for integrations with OpenSearch service
 * */
@Profile("!mockedOS")
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
	
	
	private List<OpenSearchApiObserver> observers;
	private int docCounter=0;

	public void setObserver(OpenSearchApiObserver o) {
		synchronized (observers){
			if (this.observers != null) {
				this.observers.clear();
			}
		}
		addObserver(o);
	}
	
	public void addObserver(OpenSearchApiObserver o) {
		synchronized (observers){
			if (this.observers == null) {
				this.observers = new ArrayList<>();
			}
		}
		
		this.observers.add(o);
	}
	
	/**
	 * Construct and executes a multi-search query searching for document
	 * with the given uid value within the input date range
	 * @param uid The uid to use for the multi-search query
	 * @param dateFrom The period start date
	 * @param dateTo The period end date
	 * @return The documents list contained into the Opensearch response
	 * */
	public int getAnonymizedLogsByUid(String uid, LocalDate dateFrom, LocalDate dateTo, OutputStream out){
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
		return extractDocumentsFromOpensearch(query, out);
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
	public int getAnonymizedLogsByTraceId(String traceId, LocalDate dateFrom, LocalDate dateTo, OutputStream out){
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

		return extractDocumentsFromOpensearch(query, out);
	}
	
	/**
	 * Construct and executes a multi-search query searching for document
	 * with the given jti value within the input date range
	 * @param jti The jti value to use for the multi-search query
	 * @param dateFrom The period start date
	 * @param dateTo The period end date
	 * @return The documents list contained into the Opensearch response
	 * */
	public int getAnonymizedSessionLogsByJti(String jti, LocalDate dateFrom, LocalDate dateTo, OutputStream out){
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

		return extractDocumentsFromOpensearch(query, out);
		
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
        
        int counter = 0;
        ArrayList<String> currentDocs;
        try {
	        while (!( currentDocs = getDocumentsFromCurrentResponse(response)).isEmpty()){
	        	boolean show=true;
	        	for(String line : currentDocs) {
	        		out.write(line.getBytes(StandardCharsets.UTF_8));
	        		if (show) {
	        			log.info("first line ... "+line);
	        			show=false;
	        		}
	        		counter ++;
	        	}
	        	out.flush();
	        	log.info("Fetching next page from OpenSearch...");
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
							String doc = opensearchEnrichedDoc.getJSONObject(hitIndex).getJSONObject("_source").toString();
							docCounter++;
							documents.add(doc);
							this.observers.parallelStream().forEach(o -> o.notify(doc, docCounter));
						}
					}
				}
			}
		}
		return documents;
	}
}
