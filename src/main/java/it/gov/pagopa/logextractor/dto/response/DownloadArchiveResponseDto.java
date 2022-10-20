package it.gov.pagopa.logextractor.dto.response;

import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadArchiveResponseDto extends BaseResponseDTO {

	private String password;
	private byte[] zip;

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
