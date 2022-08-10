package it.gov.pagopa.logextractor.dto.response;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GetBasicDataResponseDto extends BaseResponseDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private String data;
	
	@Builder
	public GetBasicDataResponseDto(String message, String data) {
		super(message);
		this.data = data;
	}
}
