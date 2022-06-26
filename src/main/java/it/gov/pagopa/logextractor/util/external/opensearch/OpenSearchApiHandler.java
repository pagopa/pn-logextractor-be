package it.gov.pagopa.logextractor.util.external.opensearch;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Uility class for integrations with OpenSearch service
 * */
@Component
public class OpenSearchApiHandler {
	
	@Autowired
	@Qualifier("openSearchRestTemplate")
	RestTemplate client;

	/**
	 * Performs a multi-search HTTP GET request to the Opensearch service
	 * @param query The mmulti-search query to the sent
	 * @param host The base URL of the Opensearch service
	 * @param basicAuthUsername The username for the basic authentication
	 * @param basicAuthPassword The password for the basic authentication
	 * @return The documents list contained into the Opensearch response
	 * */
	public ArrayList<String> getDocumentsByMultiSearchQuery(String query, String host, String basicAuthUsername, String basicAuthPassword) {
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setBasicAuth(basicAuthUsername, basicAuthPassword);
        List<MediaType> acceptedTypes = new ArrayList<MediaType>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HttpEntity<String> request = new HttpEntity<String>(query, requestHeaders);
        ResponseEntity<String> response = client.exchange(host+"/_msearch", HttpMethod.GET, request, String.class);
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
        	JSONObject obj = responsesObject.getJSONObject(index).getJSONObject("hits");
        	JSONArray opensearchEnrichedDoc = obj.getJSONArray("hits");
        	for(int hitIndex = 0; hitIndex < opensearchEnrichedDoc.length(); hitIndex++) {
        		documents.add(opensearchEnrichedDoc.getJSONObject(hitIndex).getJSONObject("_source").toString());
        	}
        }
        return documents;
	}
}
