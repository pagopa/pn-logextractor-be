package it.gov.pagopa.logextractor.dto.response;

import java.util.ArrayList;

import it.gov.pagopa.logextractor.dto.NotificationDetailsNotificationStatusHistoryData;
import it.gov.pagopa.logextractor.dto.NotificationDetailsTimelineData;
import lombok.Getter;

@Getter
public class NotificationHistoryResponseDTO {

	private String notificationStatus;
	private ArrayList<NotificationDetailsNotificationStatusHistoryData> notificationStatusHistory;
	private ArrayList<NotificationDetailsTimelineData> timeline;
}
