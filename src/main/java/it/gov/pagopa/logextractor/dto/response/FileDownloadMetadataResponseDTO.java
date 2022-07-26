package it.gov.pagopa.logextractor.dto.response;

import it.gov.pagopa.logextractor.dto.FileDownloadInfo;
import lombok.Getter;

@Getter
public class FileDownloadMetadataResponseDTO {

	private String key;
	private String versionId;
	private String documentType;
	private String documentStatus;
	private String contentType;
	private Integer contentLength;
	private String checksum;
	private String retentionUntil;
	private FileDownloadInfo download;
}
