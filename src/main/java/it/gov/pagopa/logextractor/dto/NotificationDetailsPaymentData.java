package it.gov.pagopa.logextractor.dto;

import lombok.Getter;

@Getter
public class NotificationDetailsPaymentData {

	private NotificationDetailsPaymentTypeData pagoPaForm;
	private NotificationDetailsPaymentTypeData f24flatRate;
	private NotificationDetailsPaymentTypeData f24standard;
}
