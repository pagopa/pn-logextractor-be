package it.gov.pagopa.logextractor.util.opensearch;

import java.util.ArrayList;
//import java.util.Base64;
import java.util.List;

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

	//TODO: Add return type to get documents after performing query
	public void getDocuments(ArrayList<OpenSearchQuerydata> queryData, boolean isBooleanQuery, String url, 
								String basicAuthUsername, String basicAuthPassword) {
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
//		String authStr = "admin:admin";
//	    String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes());
		RestTemplate client = (RestTemplate) ApplicationContextProvider.getBean("restTemplate");
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setBasicAuth(basicAuthUsername, basicAuthPassword);// add("Authorization", "Basic " + base64Creds);
        List<MediaType> acceptedTypes = new ArrayList<MediaType>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HttpEntity<String> request = new HttpEntity<String>(queryConstructor.createMultiSearchQuery(queryData, isBooleanQuery), requestHeaders);
        client.exchange(url, HttpMethod.GET, request, Void.class);
	}
}
