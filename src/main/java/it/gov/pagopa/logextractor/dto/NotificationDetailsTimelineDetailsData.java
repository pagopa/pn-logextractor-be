package it.gov.pagopa.logextractor.dto;

import java.util.ArrayList;

import lombok.Getter;

@Getter
public class NotificationDetailsTimelineDetailsData {

	private Integer recIndex;
	private NotificationHistoryPhysicalAddressData physicalAddress;
	private NotificationHistoryDigitalAddressData digitalAddress;
	private String digitalAddressSource;
	private Boolean isAvailable;
	private String attemptDate;
	private String deliveryMode;
	private String contactPhase;
	private Integer sentAttemptMade;
	private String sendDate;
	private ArrayList<String> errors;
	private String lastAttemptDate;
	private Integer retryNumber;
	private NotificationHistoryDownstreamIdData downstreamId;
	private String responseStatus;
	private String notificationDate;
	private String serviceLevel;
	private Boolean investigation;
	private Integer numberOfPages;
	private NotificationHistoryPhysicalAddressData newAddress;
	private String generatedAarUrl;
	private String reasonCode;
	private String reason;
}
