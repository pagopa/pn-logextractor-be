package it.gov.pagopa.logextractor.exception;

import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2607661026693609625L;
	private BaseResponseDto dto;

	public CustomException(String msg) {
		super(msg);
	}
}
