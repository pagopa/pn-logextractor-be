package it.gov.pagopa.logextractor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NotificationAttachmentDownloadMetadataResponseDto {
	private String filename;
	private String contentType;
	private Integer contentLength;
	private String sha256;
	private String url;
	private Integer retryAfter;
}
