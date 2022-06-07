package it.gov.pagopa.logextractor.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NotificationCsvBean {

	private String iun;
	private String sendDate;
	private String attestationGenerationDate;
	private String subject;
	private List<String> taxIds;
}
