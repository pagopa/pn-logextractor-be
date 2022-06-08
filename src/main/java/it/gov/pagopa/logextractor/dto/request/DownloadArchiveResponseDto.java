package it.gov.pagopa.logextractor.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DownloadArchiveResponseDto {

	private String password;
	private byte[] zip;
}
