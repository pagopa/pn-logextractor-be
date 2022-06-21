package it.gov.pagopa.logextractor.dto.response;



import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetBasicDataResponseDto implements Serializable {
	private String data;
}
