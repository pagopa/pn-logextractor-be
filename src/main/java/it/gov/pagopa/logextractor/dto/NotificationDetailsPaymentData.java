package it.gov.pagopa.logextractor.dto;

import lombok.Getter;

@Getter
public class NotificationDetailsPaymentData {

	private NotificationDetailsPaymentTypeData pagoPa;
	private F24Payment f24;
}
