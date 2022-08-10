package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import it.gov.pagopa.logextractor.util.ValidationConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TraceIdLogsRequestDto extends BaseRequestDto {
	@NotBlank
	private String traceId;
	
	@NotBlank
	@Pattern(regexp = ValidationConstants.INPUT_DATE_FORMAT) 
	private String dateFrom;
	
	@NotBlank
	@Pattern(regexp = ValidationConstants.INPUT_DATE_FORMAT) 
	private String dateTo;
}
