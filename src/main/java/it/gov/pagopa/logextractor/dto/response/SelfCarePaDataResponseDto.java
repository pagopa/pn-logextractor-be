package it.gov.pagopa.logextractor.dto.response;

import it.gov.pagopa.logextractor.dto.PaGeneralContacts;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SelfCarePaDataResponseDto {

	private String id;
	private String name;
	private String taxId;
	private PaGeneralContacts generalContacts;
}
