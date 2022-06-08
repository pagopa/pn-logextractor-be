package it.gov.pagopa.logextractor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProblemError {

	private String code;
	private String element;
	private String detail;
}