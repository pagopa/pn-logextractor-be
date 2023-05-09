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

	private Integer code;
	
	public CustomException(String msg) {
		super(msg);
		this.code = 500;
	}
	public CustomException(String msg, Integer code) {
		super(msg);
		this.code = code;
	}
}
