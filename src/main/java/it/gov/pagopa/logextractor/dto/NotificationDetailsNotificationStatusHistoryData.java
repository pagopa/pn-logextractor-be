package it.gov.pagopa.logextractor.dto;

import java.util.ArrayList;

import lombok.Getter;

@Getter
public class NotificationDetailsNotificationStatusHistoryData {

	private String status;
	private String activeFrom;
	private ArrayList<String> relatedTimelineElements;
}
