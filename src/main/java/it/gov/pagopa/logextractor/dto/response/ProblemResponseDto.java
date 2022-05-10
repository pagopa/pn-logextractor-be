package it.gov.pagopa.logextractor.dto.response;

import java.util.ArrayList;

import it.gov.pagopa.logextractor.dto.ProblemError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProblemResponseDto {

	int status;
	String title;
	String detail;
	String traceId;
	ArrayList<ProblemError> errors;
}
