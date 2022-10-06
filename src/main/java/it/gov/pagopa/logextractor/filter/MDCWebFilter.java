package it.gov.pagopa.logextractor.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.RandomUtils;
import it.gov.pagopa.logextractor.util.external.cognito.CognitoApiHandler;

/**
 * WebFilter that puts in the MDC log map a unique identifier for incoming requests.
 */
@Component
public class MDCWebFilter extends OncePerRequestFilter {

	@Autowired
	private CognitoApiHandler cognitoApiHandler;
	
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		
    	if(StringUtils.isBlank(request.getHeader("Auth"))) {
    		throw new RuntimeException("No Auth header found for current request: " + request.getRequestURI());
    	}
        try {
        	MDC.put("trace_id", new RandomUtils().generateRandomTraceId());
        	MDC.put("user_identifier", cognitoApiHandler.getUserIdentifier(request.getHeader("Auth")));
            filterChain.doFilter(request, response);
        } catch (LogExtractorException e) {
        	throw new RuntimeException("Exception retrieving user identifier");
		} finally {
            MDC.remove("user_identifier");
        }
    }
    
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return "/logextractor/v1/health-check/status".equals(request.getRequestURI());
	}
}