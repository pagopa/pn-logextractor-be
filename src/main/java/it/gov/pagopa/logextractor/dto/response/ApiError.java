package it.gov.pagopa.logextractor.dto.response;

public class ApiError {
	String message;
	
	public ApiError(Exception serverException) {
		this.message = serverException.getMessage();
	}
	
	public ApiError(String message) {
		this.message = message;
	}
}