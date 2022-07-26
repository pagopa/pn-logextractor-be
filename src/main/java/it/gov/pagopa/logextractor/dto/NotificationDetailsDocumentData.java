package it.gov.pagopa.logextractor.dto;

import lombok.Getter;

@Getter
public class NotificationDetailsDocumentData {

	private NotificationDetailsDigestsData digests;
	private String contentType;
	private NotificationDetailsRefData ref;
	private String title;
	private Boolean requiresAck;
	private Boolean sendByMail;
	private String docIdx;
}
