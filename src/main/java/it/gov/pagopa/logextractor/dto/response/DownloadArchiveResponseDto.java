package it.gov.pagopa.logextractor.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class DownloadArchiveResponseDto {

	private String password;
	private byte[] zip;
}
