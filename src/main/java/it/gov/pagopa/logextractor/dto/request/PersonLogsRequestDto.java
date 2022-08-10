package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import it.gov.pagopa.logextractor.annotation.PeriodOf3Months;
import it.gov.pagopa.logextractor.annotation.PersonLogsFields;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.ValidationConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PeriodOf3Months
@PersonLogsFields
public class PersonLogsRequestDto extends BaseRequestDto {
	
    private RecipientTypes recipientType;
	private boolean deanonimization;
	
	@Pattern(regexp = ValidationConstants.FISCAL_CODE_PATTERN) 
	private String taxId;
	
	private String personId;
	
	@Pattern(regexp = ValidationConstants.IUN_PATTERN)
	@Size(min = 25, max = 25)
	private String iun;
	
	@Pattern(regexp = ValidationConstants.INPUT_DATE_FORMAT)
	private String dateFrom;
	
	@Pattern(regexp = ValidationConstants.INPUT_DATE_FORMAT)
	private String dateTo;
}
