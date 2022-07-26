package it.gov.pagopa.logextractor.dto;

import java.util.ArrayList;

import lombok.Getter;

@Getter
public class NotificationData {
	private String iun;
	private String paProtocolNumber;
	private String sender;
	private String sentAt;
	private String subject;
	private String notificationStatus;
	private ArrayList<String> recipients;
}
