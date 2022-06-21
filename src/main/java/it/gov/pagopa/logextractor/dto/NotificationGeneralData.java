package it.gov.pagopa.logextractor.dto;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class NotificationGeneralData {

	private String iun;
	private String sentAt;
	private String subject;
	private ArrayList<String> recipients;
}
