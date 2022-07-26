package it.gov.pagopa.logextractor.util.external.cognito;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonymizationApiHandler;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
/**
 * Uility class for integrations with Cognito service
 * */
public class CognitoApiHandler {

	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	@Value("${external.aws.cognito.region}")
	String cognitoRegion;
	
	@Value("${external.aws.cognito.user.url}")
	String cognitoUserUrl;
	
	@Autowired
	DeanonymizationApiHandler deanonimizationHandler;
	
	/**
	 * Performs a GET HTTP request to the Cognito user pool to get the logged in user identifier
	 * @param accessToken The logged in user access token for Cognito
	 * @return The user identifier
	 * */
	public String getUserIdentifier(String accessToken) {
		String url = String.format(cognitoUserUrl, cognitoRegion);
		JSONObject requestBody = new JSONObject();
		requestBody.put("AccessToken", accessToken);
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Content-Type", "application/x-amz-json-1.1");
        requestHeaders.set("X-Amz-Target", "AWSCognitoIdentityProviderService.GetUser");
        requestHeaders.set("Content-Length",String.valueOf(accessToken.getBytes().length));
        HttpEntity<String> request = new HttpEntity<String>(requestBody.toString(), requestHeaders);
        String response = client.postForObject(url, request, String.class);
        String identifier = getUserUniqueIdentifier(response);
        if(StringUtils.isBlank(identifier)) {
    		throw new RuntimeException("Exception in " + MDC.get("trace_id") + " process, no identifier for logged in user");
    	}
        log.info("Getting user identifier...");
        long serviceStartTime = System.currentTimeMillis();
        String userIdentifier = deanonimizationHandler.getUniqueIdentifierForPerson(RecipientTypes.PF, identifier);
        log.info("User identifier retrieved in {} ms", System.currentTimeMillis() - serviceStartTime);
        return userIdentifier;
	}
	
	/**
	 * Extracts the user identifier from the Cognito response
	 * @param userAttributes The user attributes list
	 * @return The user identifier
	 * */
	private String getUserUniqueIdentifier(String userAttributes) {
		JSONArray attributes = new JSONObject(userAttributes).getJSONArray("UserAttributes");
		for(int objIndex = 0; objIndex <attributes.length(); objIndex++) {
			JSONObject currentAttribute = attributes.getJSONObject(objIndex);
			String currentKey = currentAttribute.getString("Name");
			if((Constants.COGNITO_CUSTOM_ATTRIBUTE_PREFIX + "log_identifier").equalsIgnoreCase(currentKey)) {
				return currentAttribute.getString("Value");
			}
		}
		return null;
	}
}
