package it.gov.pagopa.logextractor.filter;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import it.gov.pagopa.logextractor.service.LogServiceImpl;
import it.pagopa.pn.commons.log.MDCWebFilter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * WebFilter that puts in the MDC log map a unique identifier for incoming requests.
 */
@Component
@Slf4j
public class MDCTraceIdWebFilter extends MDCWebFilter {
	
	
	@Override
    public @NotNull Mono<Void> filter(@NotNull ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
		log.info("filtro per intercettare trace_id");
		HttpHeaders requestHeaders = serverWebExchange.getRequest().getHeaders();
		List<String> traceIdHeaders = requestHeaders.get("X-Amzn-Trace-Id");
		for (String t: traceIdHeaders) {
			log.info("X-Amzn-Trace-Id:{}",t);
		}
		return super.filter(serverWebExchange, webFilterChain);
	}


}