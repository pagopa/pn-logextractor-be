package it.gov.pagopa.logextractor.dto.response;

import java.util.ArrayList;

import it.gov.pagopa.logextractor.dto.NotificationData;
import lombok.Getter;

@Getter
public class NotificationsGeneralDataResponseDto {

	private ArrayList<NotificationData> resultsPage;
	private Boolean moreResult;
	private ArrayList<String> nextPagesKey;
}
