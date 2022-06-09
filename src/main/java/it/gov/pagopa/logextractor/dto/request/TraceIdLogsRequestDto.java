package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.Pattern;

import it.gov.pagopa.logextractor.util.Constants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TraceIdLogsRequestDto extends BaseRequestDto {
	private String traceId;
	@Pattern(regexp = Constants.INPUT_DATE_FORMAT) 
	private String dateFrom;
	@Pattern(regexp = Constants.INPUT_DATE_FORMAT) 
	private String dateTo;
}
