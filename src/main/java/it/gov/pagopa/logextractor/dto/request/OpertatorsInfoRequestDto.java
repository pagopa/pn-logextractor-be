package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import it.gov.pagopa.logextractor.util.Constants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpertatorsInfoRequestDto extends BaseRequestDto {

	@NotBlank
	@Pattern(regexp = Constants.INPUT_DATE_FORMAT) 
	private String dateFrom;
	@NotBlank
	@Pattern(regexp = Constants.INPUT_DATE_FORMAT) 
	private String dateTo;
	@NotBlank
	@Pattern(regexp = Constants.FISCAL_CODE_PATTERN)
	private String taxId;
}
