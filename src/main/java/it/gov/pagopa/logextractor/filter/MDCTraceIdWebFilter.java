package it.gov.pagopa.logextractor.filter;

import org.springframework.stereotype.Component;

import it.pagopa.pn.commons.log.MDCWebFilter;

/**
 * WebFilter that puts in the MDC log map a unique identifier for incoming requests.
 */
@Component
public class MDCTraceIdWebFilter extends MDCWebFilter {


}