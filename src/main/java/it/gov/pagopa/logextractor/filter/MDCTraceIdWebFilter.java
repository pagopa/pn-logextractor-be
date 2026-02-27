package it.gov.pagopa.logextractor.filter;

import it.gov.pagopa.logextractor.util.RandomUtils;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * WebFilter that puts in the MDC log map a unique identifier for incoming requests.
 */
@Component
@Slf4j
public class MDCTraceIdWebFilter  extends OncePerRequestFilter {

	
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
    		String t = request.getHeader("X-Amzn-Trace-Id");
			log.info("X-Amzn-Trace-Id:{}",t);
    		t = t != null ? t : RandomUtils.generateRandomTraceId();
			MDC.put(LoggingConstants.TRACE_ID_PLACEHOLDER, t);
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