package it.gov.pagopa.logextractor.util.opensearch;

import java.util.ArrayList;
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

	public void getDocuments(ArrayList<OpenSearchQuerydata> queryData, boolean isBooleanQuery, String url) {
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		RestTemplate client = (RestTemplate) ApplicationContextProvider.getBean("restTemplate");
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> acceptedTypes = new ArrayList<MediaType>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HttpEntity<String> request = new HttpEntity<String>(queryConstructor.createMultiSearchQuery(queryData, isBooleanQuery), requestHeaders);
        client.exchange(url, HttpMethod.GET, request, Void.class);
	}
}
