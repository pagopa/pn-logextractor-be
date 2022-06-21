package it.gov.pagopa.logextractor.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class NotificationCsvBean {

	private String iun;
	private String sendDate;
	private String attestationGenerationDate;
	private String subject;
	private String taxIds;
}
