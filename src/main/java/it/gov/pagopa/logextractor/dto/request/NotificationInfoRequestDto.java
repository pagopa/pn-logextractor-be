package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationInfoRequestDto extends BaseRequestDto {

	@NotBlank
	private String iun;
}
