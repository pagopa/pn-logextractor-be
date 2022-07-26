package it.gov.pagopa.logextractor.dto;

import java.util.ArrayList;

import lombok.Getter;

@Getter
public class NotificationDetailsTimelineData {

	private String elementId;
	private String timestamp;
	private ArrayList<NotificationDetailsTimelineLegalFactsData> legalFactsIds;
	private String category;
	private NotificationDetailsTimelineDetailsData details; 
}
