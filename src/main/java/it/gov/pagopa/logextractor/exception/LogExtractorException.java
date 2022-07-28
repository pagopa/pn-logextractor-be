package it.gov.pagopa.logextractor.exception;

public class LogExtractorException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public LogExtractorException(String errorMessage) {
        super(errorMessage);
    }
}
