package it.gov.pagopa.logextractor.dto.response;

import it.gov.pagopa.logextractor.dto.FileDownloadInfo;
import lombok.Getter;

@Getter
public class FileDownloadMetadataResponseDto {
	private FileDownloadInfo download;
}
