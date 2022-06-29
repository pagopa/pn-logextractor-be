package it.gov.pagopa.logextractor.util.external.selfcare;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Uility class for integrations with Self care service
 * */
@Component
public class SelfCareApiHandler {

	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the general data of the notifications managed within a period
	 * @param url The PN external service base URL
	 * @param ipaCode The public authority code
	 * @return The list of notifications' general data
	 * */
	@Cacheable(cacheNames="services")
	public String getEncodedIpaCode(String url, String ipaCode) {
        ResponseEntity<String> response = client.getForEntity(url, String.class);
        return getIpaCode(response.getBody(), ipaCode);
	}
	
	/**
	 * Extracts the public authority encoded code from the pn service response
	 * @param pnResponse The PN external service response
	 * @param ipaCode The public authority code
	 * @return The list of notifications' general data
	 * */
	public String getIpaCode(String pnResponse, String ipaCode) {
		JSONArray jsonResponse = new JSONObject(pnResponse).getJSONArray("PaSummariesList");
		for(int index = 0; index < jsonResponse.length(); index++) {
			JSONObject currentObj = jsonResponse.getJSONObject(index);
			if(ipaCode.equals(currentObj.getString("name"))) {
				return currentObj.getString("id");
			}
		}
		return null;
	}
}
