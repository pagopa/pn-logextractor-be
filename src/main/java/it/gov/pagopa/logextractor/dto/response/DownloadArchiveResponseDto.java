package it.gov.pagopa.logextractor.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DownloadArchiveResponseDto extends BaseResponseDTO {

	private String password;
	private byte[] zip;
	
	@Builder
	public DownloadArchiveResponseDto(String message, String password, byte[] zip) {
		super(message);
		this.password = password;
        this.zip = zip;
	}
}
