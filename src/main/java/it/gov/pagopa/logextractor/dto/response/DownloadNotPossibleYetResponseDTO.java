package it.gov.pagopa.logextractor.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class DownloadNotPossibleYetResponseDTO {

	private String message;
}
