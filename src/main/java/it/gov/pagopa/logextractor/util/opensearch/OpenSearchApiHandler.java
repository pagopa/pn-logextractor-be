package it.gov.pagopa.logextractor.util.opensearch;

import java.util.ArrayList;
//import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import it.gov.pagopa.logextractor.config.ApplicationContextProvider;

/**
 * Uility class for integrations with OpenSearch service
 * */
public class OpenSearchApiHandler {

	/**
	 * Performs a multi-search HTTP GET request to the Opensearch service*/
	public ArrayList<String> getDocumentsByMultiSearchQuery(ArrayList<OpenSearchQuerydata> queryData, boolean isBooleanQuery, String host, 
								String basicAuthUsername, String basicAuthPassword) {
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		RestTemplate client = (RestTemplate) ApplicationContextProvider.getBean("restTemplate");
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setBasicAuth(basicAuthUsername, basicAuthPassword);
        List<MediaType> acceptedTypes = new ArrayList<MediaType>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HttpEntity<String> request = new HttpEntity<String>(queryConstructor.createMultiSearchQuery(queryData, isBooleanQuery), requestHeaders);
        var response = client.exchange(host+"/_msearch", HttpMethod.GET, request, String.class);
        return getDocuments(response.getBody());
	}
	
	/**
	 * Gets the document list from the Opensearch response
	 * @param openSearchResponseBody The Opensearch response
	 * @return The decouments list
	 * */
	private ArrayList<String> getDocuments(String openSearchResponseBody) {
		ArrayList<String> documents = new ArrayList<String>();
		JSONArray responsesObject = new JSONObject(openSearchResponseBody).getJSONArray("responses");
        for(int index = 0; index < responsesObject.length(); index++) {
        	JSONArray opensearchEnrichedDoc = responsesObject.getJSONObject(index).getJSONObject("hits").getJSONArray("hits");
        	for(int hitIndex = 0; hitIndex < opensearchEnrichedDoc.length(); hitIndex++) {
        		documents.add(opensearchEnrichedDoc.getJSONObject(hitIndex).getJSONObject("_source").toString());
        	}
        }
        return documents;
	}
}
