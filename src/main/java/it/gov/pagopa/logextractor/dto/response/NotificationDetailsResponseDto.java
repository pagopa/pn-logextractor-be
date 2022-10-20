package it.gov.pagopa.logextractor.dto.response;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.logextractor.dto.NotificationDetailsDocumentData;
import it.gov.pagopa.logextractor.dto.NotificationDetailsRecipientsData;
import lombok.Getter;

@Getter
public class NotificationDetailsResponseDto {

	@JsonProperty("abstract")
	private String abstractDetail;
	private String idempotenceToken;
	private String paProtocolNumber;
	private String subject;
	private ArrayList<NotificationDetailsRecipientsData> recipients;
	private ArrayList<NotificationDetailsDocumentData> documents;
	private String notificationFeePolicy;
	private String cancelledIun;
	private String physicalCommunicationType;
	private String senderDenomination;
	private String senderTaxId;
	private String group;
	private Integer amount;
	private String senderPaId;
	private String iun;
	private String sentAt;
	private String cancelledByIun;
	private Boolean documentsAvailable;
	private String paymentExpirationDate;
}
