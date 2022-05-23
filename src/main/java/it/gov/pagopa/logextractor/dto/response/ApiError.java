package it.gov.pagopa.logextractor.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiError {
	private String message;
	
	public ApiError(Exception serverException) {
		this.message = serverException.getMessage();
	}
	
	public ApiError(String message) {
		this.message = message;
	}
}