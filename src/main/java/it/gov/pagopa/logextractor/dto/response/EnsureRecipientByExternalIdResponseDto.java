package it.gov.pagopa.logextractor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EnsureRecipientByExternalIdResponseDto {

	String internalId;
}
