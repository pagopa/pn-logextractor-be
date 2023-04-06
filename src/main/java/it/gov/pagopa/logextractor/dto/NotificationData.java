package it.gov.pagopa.logextractor.dto;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationData {
	private String iun;
	private String sentAt;
	private String subject;
	private String requestAcceptedAt;
	private ArrayList<String> recipients;
}
