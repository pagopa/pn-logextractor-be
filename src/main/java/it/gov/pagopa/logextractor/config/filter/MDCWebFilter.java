package it.gov.pagopa.logextractor.config.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.springframework.web.filter.OncePerRequestFilter;
import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.CommonUtilities;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;

/**
 * WebFilter that puts in the MDC log map a unique identifier for incoming requests.
 */
@Component
public class MDCWebFilter extends OncePerRequestFilter {

	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	@Value("${external.aws.cognito.region}")
	String cognitoRegion;
	
	@Value("${external.aws.cognito.user.url}")
	String cognitoUserUrl;
	
	@Value("${external.denomination.ensureRecipientByExternalId.url}")
	String getUniqueIdURL;
	
	@Autowired
	DeanonimizationApiHandler deanonimizationHandler;
	
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		
    	if(StringUtils.isBlank(request.getHeader("Auth"))) {
    		throw new RuntimeException("No Auth header found for current request: " + request.getRequestURI());
    	}
        try {
        	MDC.put("trace_id", new CommonUtilities().generateRandomTraceId());
        	MDC.put("user_identifier", getUserIdentifier(request.getHeader("Auth")));
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("trace_id");
            MDC.remove("user_identifier");
        }
    }
    
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return "/logextractor/v1/health-check/status".equals(request.getRequestURI());
	}

	private String getUserIdentifier(String accessToken) {
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
        return deanonimizationHandler.getUniqueIdentifierForPerson(RecipientTypes.PF, identifier, getUniqueIdURL).getData();
	}
	
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