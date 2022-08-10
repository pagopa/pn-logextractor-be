package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.util.ValidationConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonPersonIdRequestDto extends BaseRequestDto{

	@RecipientType
	private String recipientType;
	@NotBlank
	@Size(min = 16, max = 16) 
	@Pattern(regexp = ValidationConstants.FISCAL_CODE_PATTERN, message = "Invalid Tax ID") 
	private String taxId;
}
