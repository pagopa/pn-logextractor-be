package it.gov.pagopa.logextractor.dto.response;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.logextractor.dto.NotificationDetailsDocumentData;
import it.gov.pagopa.logextractor.dto.NotificationDetailsRecipientsData;
import lombok.Getter;

@Getter
public class NotificationDetailsResponseDto {

	private ArrayList<NotificationDetailsRecipientsData> recipients;
	private ArrayList<NotificationDetailsDocumentData> documents;
	private String sentAt;
}
