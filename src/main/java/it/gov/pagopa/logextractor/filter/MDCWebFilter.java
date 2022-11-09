package it.gov.pagopa.logextractor.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.logextractor.util.constant.CognitoConstants;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.RandomUtils;
import it.gov.pagopa.logextractor.util.external.cognito.CognitoApiHandler;

/**
 * WebFilter that puts in the MDC log map a unique identifier for incoming requests.
 */
@Slf4j
@Component
public class MDCWebFilter extends OncePerRequestFilter {

	@Autowired
	private CognitoApiHandler cognitoApiHandler;

	@Autowired
	@Qualifier("simpleObjectMapper")
	private ObjectMapper mapper;
	
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
			MDC.put(LoggingConstants.TRACE_ID_PLACEHOLDER, new RandomUtils().generateRandomTraceId());
			if(StringUtils.isBlank(request.getHeader("Auth"))) {
				throw new LogExtractorException("No Auth header found for current request: " + request.getRequestURI());
			}
			log.info("Getting user identifier...");
			long serviceStartTime = System.currentTimeMillis();
        	MDC.put(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER, cognitoApiHandler.getUserIdentifier(request.getHeader("Auth")));
			long performanceMillis = System.currentTimeMillis() - serviceStartTime;
			log.info("User identifier retrieved in {} ms", performanceMillis);
			MDC.put(LoggingConstants.VALIDATION_TIME, String.valueOf(performanceMillis));
            filterChain.doFilter(request, response);
        } catch (LogExtractorException e) {
			log.error(ExceptionUtils.getStackTrace(e));
			sendErrorResponse(response, ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_MESSAGE);
		} finally {
            MDC.remove(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER);
			MDC.remove(LoggingConstants.TRACE_ID_PLACEHOLDER);
			MDC.remove(LoggingConstants.VALIDATION_TIME);
        }
    }
    
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return "/status".equals(request.getRequestURI());
	}

	private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
		Map<String, Object> errorDetails = new HashMap<>();
		errorDetails.put(GenericConstants.ERROR_MESSAGE_KEY, errorMessage);
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		mapper.writeValue(response.getWriter(), errorDetails);
	}
}