package it.gov.pagopa.logextractor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationAttachmentDownloadMetadataResponseDto {
	private String filename;
	private String contentType;
	private Integer contentLength;
	private String sha256;
	private String url;
	private Integer retryAfter;
}
