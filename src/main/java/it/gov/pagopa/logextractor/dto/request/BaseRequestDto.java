package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import it.gov.pagopa.logextractor.util.Constants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseRequestDto {

	@NotBlank
	protected String ticketNumber;
	@NotBlank
	@Pattern(regexp = Constants.PASSWORD_PATTERN)
	protected String password;
}
