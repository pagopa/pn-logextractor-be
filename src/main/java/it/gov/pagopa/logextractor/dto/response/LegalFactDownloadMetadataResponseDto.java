package it.gov.pagopa.logextractor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LegalFactDownloadMetadataResponseDto {

	private String filename;
	private Integer contentLength;
	private String url;
	private Integer retryAfter;
}
