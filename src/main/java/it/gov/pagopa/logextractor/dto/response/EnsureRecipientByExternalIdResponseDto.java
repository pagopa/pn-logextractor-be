package it.gov.pagopa.logextractor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnsureRecipientByExternalIdResponseDto {

	private String internalId;
}
