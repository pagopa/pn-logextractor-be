package it.gov.pagopa.logextractor.config.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import it.gov.pagopa.logextractor.util.RandomGenerator;

/**
 * This WebFilter puts in the MDC log map a unique identifier for incoming requests.
 */
@Component
public class MDCTraceIdWebFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    	RandomGenerator traceIdGenerator = new RandomGenerator();
    	MDC.put("trace_id", traceIdGenerator.generateRandomTraceId());
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("trace_id");
        }
    }
}