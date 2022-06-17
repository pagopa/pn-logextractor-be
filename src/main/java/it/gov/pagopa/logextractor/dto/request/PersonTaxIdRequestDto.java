package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import it.gov.pagopa.logextractor.util.Constants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonTaxIdRequestDto {
	
	@Size(min = 1, max = 100, message = "Invalid person id") 
	@Pattern(regexp = Constants.INTERNAL_ID_PATTERN)
	@NotBlank
	private String personId;
}
