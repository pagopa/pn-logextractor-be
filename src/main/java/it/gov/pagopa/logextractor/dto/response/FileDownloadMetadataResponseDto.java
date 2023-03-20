package it.gov.pagopa.logextractor.dto.response;

import it.gov.pagopa.logextractor.dto.FileDownloadInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDownloadMetadataResponseDto {
	private FileDownloadInfo download;
}
