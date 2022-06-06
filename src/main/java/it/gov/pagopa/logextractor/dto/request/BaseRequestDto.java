package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseRequestDto {

	@NotBlank
	protected String ticketNumber;
	@NotBlank
	protected String password;
}
