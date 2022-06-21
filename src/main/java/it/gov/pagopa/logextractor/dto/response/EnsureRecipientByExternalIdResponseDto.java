package it.gov.pagopa.logextractor.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnsureRecipientByExternalIdResponseDto implements Serializable {

	private String internalId;
}
