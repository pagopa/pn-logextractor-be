package it.gov.pagopa.logextractor.dto;

import lombok.Getter;

@Getter
public class NotificationDetailsTimelineDetailsData {

	private Integer recIndex;
	private String physicalAddress;
	private String digitalAddress;
	private String digitalAddressSource;
	private Boolean isAvailable;
	private String attemptDate;
	private String deliveryMode;
	private String contactPhase;
	private String sentAttemptMade;
	private String sendDate;
	private String errors;
	private String lastAttemptDate;
	private String retryNumber;
	private String downstreamId;
	private String responseStatus;
	private String notificationDate;
	private String serviceLevel;
	private String investigation;
	private String newAddress;
}
