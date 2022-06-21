package it.gov.pagopa.logextractor.dto.response;

import java.util.ArrayList;

import it.gov.pagopa.logextractor.dto.ProblemError;

public class ProblemResponseDto {

	private int status;
	private String title;
	private String detail;
	private String traceId;
	private ArrayList<ProblemError> errors;
}
