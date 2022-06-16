package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.Pattern;

import it.gov.pagopa.logextractor.annotation.PeriodOf3Months;
import it.gov.pagopa.logextractor.util.Constants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PeriodOf3Months
public class PersonLogsRequestDto extends BaseRequestDto {

	private boolean deanonimization;
	
	@Pattern(regexp = Constants.FISCAL_CODE_PATTERN) 
	private String taxId;
	
	private String personId;
	
	@Pattern(regexp = Constants.IUN_PATTERN)
	private String iun;
	
	@Pattern(regexp = Constants.INPUT_DATE_FORMAT)
	private String dateFrom;
	
	@Pattern(regexp = Constants.INPUT_DATE_FORMAT)
	private String dateTo;
}
