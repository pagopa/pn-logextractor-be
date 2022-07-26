package it.gov.pagopa.logextractor.dto;

import lombok.Getter;

@Getter
public class NotificationDetailsPaymentTypeData {

	
	private NotificationDetailsDigestsData digests;
	private String contentType;
	private NotificationDetailsRefData ref;
}
