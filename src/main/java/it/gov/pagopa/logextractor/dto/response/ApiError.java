package it.gov.pagopa.logextractor.dto.response;

import org.springframework.http.HttpStatus;

public class ApiError {
	HttpStatus status;
	String message;
	
	public ApiError(HttpStatus statusCode, String errorMessage) {
		this.status = statusCode;
	}
}
