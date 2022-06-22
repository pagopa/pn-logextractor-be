package it.gov.pagopa.logextractor.dto.request;

import lombok.Builder;
import lombok.Setter;
import lombok.ToString;

@Setter
@Builder
@ToString
public class EnsureRecipientByExternalIdRequestDto {

	private String taxId;
}
