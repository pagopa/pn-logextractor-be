package it.gov.pagopa.logextractor.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationInfoRequestDto extends BaseRequestDto {

	@NotBlank
	@Size(min = 25, max = 25)
	private String iun;
}
