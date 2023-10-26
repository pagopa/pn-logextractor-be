package it.gov.pagopa.logextractor.dto;

import java.util.ArrayList;

import lombok.Getter;

@Getter
public class NotificationDetailsTimelineData {

	private ArrayList<NotificationDetailsTimelineLegalFactsData> legalFactsIds;
	private String category;
	private NotificationDetailsTimelineDataDetail details;
}
