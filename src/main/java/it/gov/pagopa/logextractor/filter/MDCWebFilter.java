package it.gov.pagopa.logextractor.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import it.gov.pagopa.logextractor.util.RandomUtils;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;

/**
 * WebFilter that puts in the MDC log map a unique identifier for incoming requests.
 */
@Component
public class MDCWebFilter extends OncePerRequestFilter {

	
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
			MDC.put(LoggingConstants.TRACE_ID_PLACEHOLDER, RandomUtils.generateRandomTraceId());
            filterChain.doFilter(request, response);
		} finally {
			MDC.remove(LoggingConstants.TRACE_ID_PLACEHOLDER);
        }
    }
    
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return "/status".equals(request.getRequestURI());
	}
}