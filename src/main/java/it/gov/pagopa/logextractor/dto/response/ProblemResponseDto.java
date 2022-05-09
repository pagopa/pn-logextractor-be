package it.gov.pagopa.logextractor.dto.response;

import java.util.ArrayList;

import it.gov.pagopa.logextractor.dto.ProblemError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProblemResponseDto {

	int status;
	String title;
	String detail;
	String traceId;
	ArrayList<ProblemError> errors;
}
