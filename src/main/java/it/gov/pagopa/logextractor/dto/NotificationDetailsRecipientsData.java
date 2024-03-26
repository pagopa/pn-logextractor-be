package it.gov.pagopa.logextractor.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class NotificationDetailsRecipientsData {

	private List<NotificationDetailsPaymentData> payments;
}
