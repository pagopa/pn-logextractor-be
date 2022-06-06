package it.gov.pagopa.logextractor.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonLogsRequestDto {

	private String ticketNumber;
	private boolean deanonimization;
	private String taxId;
	private String personId;
	private Integer iun;
	private String dateFrom;
	private String dateTo;
	private String password;
}
