package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import it.gov.pagopa.logextractor.util.ValidationConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyNotificationsRequestDto extends BaseRequestDto {
	
	@NotBlank
	@Pattern(regexp = ValidationConstants.INPUT_MONTH_FORMAT) 
	private String referenceMonth;
	
	@NotBlank
	@Pattern(regexp = ValidationConstants.INPUT_MONTH_FORMAT)
	private String endMonth;
	
	@NotBlank
	private String publicAuthorityName;

}
