package it.gov.pagopa.logextractor.dto;

import lombok.Getter;

@Getter
public class NotificationDetailsRecipientsData {

	private String recipientType;
	private String taxId;
	private String internalId;
	private String denomination;
	private NotificationDetailsDigitalDomicileData digitalDomicile;
	private NotificationDetailsPhysicalAddressData physicalAddress;
	private NotificationDetailsPaymentData payment;
}
