package it.gov.pagopa.logextractor.dto.response;

import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class ApiError {
	
	HttpStatus status;
	String message;
}