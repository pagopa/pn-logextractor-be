package it.gov.pagopa.logextractor.dto.response;

import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadArchiveResponseDto extends BaseResponseDTO {

	private String password;
	private byte[] zip;
}
