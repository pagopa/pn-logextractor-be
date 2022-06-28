package it.gov.pagopa.logextractor.util.external.selfcare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.gov.pagopa.logextractor.config.ApplicationContextProvider;
import it.gov.pagopa.logextractor.util.JsonUtilities;

/**
 * Uility class for integrations with Selfcare service
 * */
@Component
public class SelfCareApiHandler {

	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the general data of the notifications managed within a period
	 * @param url The PN external service base URL
	 * @param startDate The period start date
	 * @param endDate The period end date
	 * @param size The maximum number of results to be retrieved
	 * @return The list of notifications' general data
	 * */
	@Cacheable(cacheNames="services")
	public String getEncodedIpaCode(String url, String ipdaCode) {	
		RestTemplate client = (RestTemplate) ApplicationContextProvider.getBean("simpleRestTemplate");
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> acceptedTypes = new ArrayList<MediaType>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ipaCode", ipdaCode);
        ResponseEntity<String> response = client.getForEntity(url, String.class, parameters);
        return JsonUtilities.getValue(response.getBody(), "internalCode");
	}
}
