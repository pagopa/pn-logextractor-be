package it.gov.pagopa.logextractor.dto.response;

import lombok.Getter;

@Getter
public class ApiError {
	private String message;
	
	public ApiError(Exception serverException) {
		this.message = serverException.getMessage();
	}
	
	public ApiError(String message) {
		this.message = message;
	}
}