package it.gov.pagopa.logextractor.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.logextractor.util.RandomUtils;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;

/**
 * WebFilter that puts in the MDC log map a unique identifier for incoming requests.
 */
//@Slf4j
@Component
public class MDCWebFilter extends OncePerRequestFilter {

	@Autowired
	@Qualifier("simpleObjectMapper")
	private ObjectMapper mapper;
	
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
			MDC.put(LoggingConstants.TRACE_ID_PLACEHOLDER, new RandomUtils().generateRandomTraceId());
//			if(StringUtils.isBlank(request.getHeader(HeaderConstants.PAGO_PA_HELPD_UID))) {
//				throw new LogExtractorException("No x-pagopa-helpd-uid header found for current request: " + request.getRequestURI());
//			}
            filterChain.doFilter(request, response);
//        } catch (LogExtractorException e) {
//			log.error(ExceptionUtils.getStackTrace(e));
//			sendErrorResponse(response, ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_MESSAGE);
		} finally {
			MDC.remove(LoggingConstants.TRACE_ID_PLACEHOLDER);
        }
    }
    
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return "/status".equals(request.getRequestURI());
	}

//	private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
//		Map<String, Object> errorDetails = new HashMap<>();
//		errorDetails.put(GenericConstants.ERROR_MESSAGE_KEY, errorMessage);
//		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//		mapper.writeValue(response.getWriter(), errorDetails);
//	}
}