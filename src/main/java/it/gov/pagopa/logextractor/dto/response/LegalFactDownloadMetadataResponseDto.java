package it.gov.pagopa.logextractor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LegalFactDownloadMetadataResponseDto {

	private String filename;
	private Integer contentLength;
	private String url;
	private Integer retryAfter;
	
	@Override
	public String toString() {
		return String.format("fileName=%s", this.filename);
	}
}
