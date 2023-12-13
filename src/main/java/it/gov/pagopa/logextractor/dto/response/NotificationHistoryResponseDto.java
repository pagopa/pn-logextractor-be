package it.gov.pagopa.logextractor.dto.response;

import java.util.ArrayList;

import it.gov.pagopa.logextractor.dto.NotificationDetailsTimelineData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationHistoryResponseDto {

	private ArrayList<NotificationDetailsTimelineData> timeline;

}
