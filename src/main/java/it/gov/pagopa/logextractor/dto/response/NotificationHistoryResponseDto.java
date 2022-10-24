package it.gov.pagopa.logextractor.dto.response;

import java.util.ArrayList;

import it.gov.pagopa.logextractor.dto.NotificationDetailsTimelineData;
import lombok.Getter;

@Getter
public class NotificationHistoryResponseDto {

	private ArrayList<NotificationDetailsTimelineData> timeline;
}
