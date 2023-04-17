package it.gov.pagopa.logextractor.exception;

import java.io.IOException;

public class LogExtractorIntegrationException extends IOException {
	private static final long serialVersionUID = 1L;
	
	public LogExtractorIntegrationException(String errorMessage) {
        super(errorMessage);
    }
}
