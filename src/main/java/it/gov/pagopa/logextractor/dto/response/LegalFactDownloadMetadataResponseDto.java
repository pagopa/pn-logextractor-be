package it.gov.pagopa.logextractor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LegalFactDownloadMetadataResponseDto {

	private String filename;
	private Integer contentLength;
	private String url;
	private Integer retryAfter;
}
