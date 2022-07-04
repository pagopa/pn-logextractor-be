package it.gov.pagopa.logextractor.util.external.pnservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import it.gov.pagopa.logextractor.dto.NotificationDetailsDocumentData;
import it.gov.pagopa.logextractor.dto.NotificationDetailsPaymentData;
import it.gov.pagopa.logextractor.dto.NotificationDetailsRefData;
import it.gov.pagopa.logextractor.dto.NotificationDetailsTimelineData;
import it.gov.pagopa.logextractor.dto.NotificationDetailsTimelineLegalFactsData;
import it.gov.pagopa.logextractor.dto.NotificationGeneralData;
import it.gov.pagopa.logextractor.dto.LegalFactBasicData;
import it.gov.pagopa.logextractor.dto.LegalFactData;
import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.dto.PaymentDocumentData;
import it.gov.pagopa.logextractor.dto.response.LegalFactDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationAttachmentDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationsGeneralDataResponseDto;

/**
 * Uility class for integrations with Piattaforma Notifiche notifcations related services
 * */
@Component
public class NotificationApiHandler {
	
	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the
	 * general data of the notifications managed within a period
	 * 
	 * @param url       The PN external service base URL
	 * @param startDate The period start date
	 * @param endDate   The period end date
	 * @param size      The maximum number of results to be retrieved
	 * @return The list of notifications' general data
	 */
	public ArrayList<NotificationGeneralData> getNotificationsByPeriod(String url, HashMap<String, Object> params, 
			String encodedIpaCode, ArrayList<NotificationGeneralData> notifications, String nextUrlKey, ArrayList<String> pages/*JSONArray pages*/, String userIdentifier) {
		HttpHeaders requestHeaders = new HttpHeaders();
	    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	    requestHeaders.set("x-pagopa-pn-cx-id", encodedIpaCode);
	    requestHeaders.set("x-pagopa-pn-uid", "HD-"+userIdentifier);
	    List<MediaType> acceptedTypes = new ArrayList<MediaType>();
	    acceptedTypes.add(MediaType.APPLICATION_JSON);
	    requestHeaders.setAccept(acceptedTypes);
	    HashMap<String, Object> parameters = new HashMap<String, Object>();
	    for (Map.Entry<String, Object> entry : params.entrySet()) {
	    	parameters.put(entry.getKey(), entry.getValue());
	    }
	    ResponseEntity<NotificationsGeneralDataResponseDto> response = client.getForEntity(url, NotificationsGeneralDataResponseDto.class, parameters);
	    if(response.getBody().getNextPagesKey() == null || !response.getBody().getMoreResult()) {
	    	return getNotificationsGeneralData(response.getBody());
	    }
	    ArrayList<String> pageKeys = response.getBody().getNextPagesKey();
	    notifications.addAll(getNotificationsGeneralData(response.getBody()));
	    for(int index = 0; index < pageKeys.size(); index++) {
	    	String nextKey = pageKeys.get(index);
	    	HashMap<String, Object> newParameters = new HashMap<String, Object>();
		    newParameters.putAll(parameters);
			newParameters.put("nextPagesKey", nextKey);
			notifications.addAll(getNotificationsByPeriod(url, newParameters, encodedIpaCode, notifications, nextKey, pageKeys, userIdentifier));
	    }
	    return notifications;
	}
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve a
	 * notification legal start date
	 * 
	 * @param url The PN external service base URL
	 * @param iun The notification IUN
	 * @return The notification legal start date
	 */
	public NotificationDetailsResponseDto getNotificationDetails(String externalServiceUrl, String iun) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> acceptedTypes = new ArrayList<MediaType>();
		acceptedTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptedTypes);
        return client.getForEntity(externalServiceUrl + "/" + iun, NotificationDetailsResponseDto.class).getBody();
	}
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the legal
	 * fact metadata of a notification
	 * 
	 * @param externalServiceUrl The PN external service base URL
	 * @param iun                the notification IUN
	 * @param legalFactId        the legal fact key
	 * @param legalFactType      the legal fact category
	 * @return a new {@link LegalFactDownloadMetadataResponseDto} instance representing the service response
	 */
	public LegalFactDownloadMetadataResponseDto getLegalFactMetadata(String externalServiceUrl, String iun,
			String legalFactId, String legalFactType) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> acceptedTypes = new ArrayList<MediaType>();
		acceptedTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptedTypes);
		String url = String.format(externalServiceUrl, iun, legalFactType, legalFactId);
		LegalFactDownloadMetadataResponseDto response = client.getForObject(url, LegalFactDownloadMetadataResponseDto.class);
		return response;
	}
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the
	 * attached documents metadata to a notification
	 * 
	 * @param externalServiceUrl The PN external service base URL
	 * @param iun                the notification IUN
	 * @param docIdx             the document id
	 * @return a new {@link NotificationAttachmentDownloadMetadataResponseDto} instance representing the service response
	 */
	public NotificationAttachmentDownloadMetadataResponseDto getNotificationDocumentsMetadata(String externalServiceUrl, String iun, String docIdx) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> acceptedTypes = new ArrayList<MediaType>();
		acceptedTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptedTypes);
		String url = String.format(externalServiceUrl, iun, docIdx);
		return client.getForObject(url,NotificationAttachmentDownloadMetadataResponseDto.class);
	}

	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the
	 * payment documents to a notification
	 * 
	 * @param externalServiceUrl The PN external service base URL
	 * @param iun                the notification IUN
	 * @param recipients         the specific recipient
	 * @param key                the payment keys for the recipient
	 * @return a new {@link NotificationAttachmentDownloadMetadataResponseDto} instance representing the service response
	 */
	public NotificationAttachmentDownloadMetadataResponseDto getPaymentDocumentsMetadata(String externalServiceUrl, String iun, Integer recipients, String key) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> acceptedTypes = new ArrayList<MediaType>();
		acceptedTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptedTypes);
		String url = String.format(externalServiceUrl, iun, recipients, key);
		NotificationAttachmentDownloadMetadataResponseDto response = client.getForObject(url,NotificationAttachmentDownloadMetadataResponseDto.class);
		return response;
	}
	
	/**
	 * Performs a GET HTTP request to an URL to retrieve a file for notification
	 * 
	 * @param url the url to which the GET HTTP request should be made. The URL is
	 *            given by a PN external service
	 * @return a byte array containing a file
	 */
	public byte[] getFile(String url) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_PDF);
		List<MediaType> acceptedTypes = new ArrayList<MediaType>();
		acceptedTypes.add(MediaType.APPLICATION_PDF);
		requestHeaders.setAccept(acceptedTypes);
		return client.getForObject(url, byte[].class);
	}
	
	/**
	 * Gets the general data of a notification
	 * 
	 * @param notificationResponse The PN external service response containing the
	 *                             notification general data
	 * @return A list containing all the notifications' general data
	 */
	private ArrayList<NotificationGeneralData> getNotificationsGeneralData(NotificationsGeneralDataResponseDto notificationResponse) {
		ArrayList<NotificationGeneralData> notificationsGeneralData = new ArrayList<NotificationGeneralData>();
		ArrayList<NotificationData> notifications =  notificationResponse.getResultsPage();
		if(null != notifications) {
			for(int index = 0; index < notifications.size(); index++) {
            	NotificationGeneralData currentNotificationData = new NotificationGeneralData();
            	currentNotificationData.setIun(notifications.get(index).getIun());
            	currentNotificationData.setSentAt(notifications.get(index).getSentAt());
            	currentNotificationData.setSubject(notifications.get(index).getSubject());
            	currentNotificationData.setRecipients(notifications.get(index).getRecipients());
            	notificationsGeneralData.add(currentNotificationData);
            }
        }
        return notificationsGeneralData;
	}
	
	/**
	 * Gets the legal start date of a notification
	 * 
	 * @param notificationInfo The PN external service response containing the
	 *                         notification details
	 * @return The notification legal start date
	 */
	public String getLegalStartDate(NotificationDetailsResponseDto notificationInfo) {
		String legalStartDate = null;
		if(null != notificationInfo.getTimeline()) {
			for(NotificationDetailsTimelineData timelineObject : notificationInfo.getTimeline()) {
				if (null != timelineObject.getCategory() && "REQUEST_ACCEPTED".equalsIgnoreCase(timelineObject.getCategory())) {
	        		legalStartDate = timelineObject.getTimestamp();
	        	}
	        }
		}
        return legalStartDate;
	}
	
	/**
	 * Method that retrieves legal fact id, legal fact type and timestamp of a
	 * notification
	 * 
	 * @param notificationInfo The PN external service response containing the
	 *                         notification details
	 * @return A map containing the notification legal fact id, type and timestamp
	 */
	public ArrayList<LegalFactData> getLegalFactIdsAndTimestamp(NotificationDetailsResponseDto notificationInfo) {
		ArrayList<LegalFactData> legalFactData = new ArrayList<LegalFactData>();
		if(null != notificationInfo.getTimeline()) {
			for (NotificationDetailsTimelineData timelineObject : notificationInfo.getTimeline()) {
				if (null != timelineObject.getCategory() && "REQUEST_ACCEPTED".equalsIgnoreCase(timelineObject.getCategory())) {
					LegalFactData currentLegalFact = new LegalFactData();
					currentLegalFact.setTimestamp(timelineObject.getTimestamp());
					if(null != timelineObject.getLegalFactsIds()) {
						for(NotificationDetailsTimelineLegalFactsData legalFactsObject : timelineObject.getLegalFactsIds()) {
							LegalFactBasicData currentBasicData = new LegalFactBasicData();
							currentBasicData.setKey(legalFactsObject.getKey());
							currentBasicData.setCategory(legalFactsObject.getCategory());
							currentLegalFact.getBasicData().add(currentBasicData);
						}
					}
					legalFactData.add(currentLegalFact);
				}
			}
		}
		return legalFactData;
	}
	
	/**
	 * Method that retrieves the doc id of the documents that are attached to the
	 * notification
	 * 
	 * @param notificationInfo The PN external service response containing the
	 *                         notification details
	 * @return A list containing the document ids
	 */
	public ArrayList<String> getDocumentIds(NotificationDetailsResponseDto notificationInfo) {
		ArrayList<String> docIdxs = new ArrayList<String>();
		if(null != notificationInfo.getDocuments()) {
			for (NotificationDetailsDocumentData doc : notificationInfo.getDocuments()) {
				docIdxs.add(doc.getDocIdx());
			}
		}
		return docIdxs;
	}
	
	/**
	 * Method that retrieves number of recipients and their payment keys
	 * 
	 * @param notificationInfo The PN external service response containing the
	 *                         notification details
	 * @return A list containing the keys of each recipient
	 */
	public ArrayList<PaymentDocumentData> getPaymentKeys(NotificationDetailsResponseDto notificationInfo) {
		ArrayList<PaymentDocumentData> paymentData = new ArrayList<PaymentDocumentData>();
		if(null != notificationInfo.getRecipients()) {
			Map<String, String> paymentKeys = new HashMap<>();
			NotificationDetailsRefData payObj = null;
			for (int recipient = 0; recipient < notificationInfo.getRecipients().size(); recipient++ ) {
				if(null !=  notificationInfo.getRecipients().get(recipient).getPayment()) {
					NotificationDetailsPaymentData paymentObject = notificationInfo.getRecipients().get(recipient).getPayment();
					if(null != paymentObject.getPagoPaForm()) {
						payObj = paymentObject.getPagoPaForm().getRef();
						paymentKeys.put("pagoPaFormKey", null != payObj.getKey() ? payObj.getKey() : null);
					}
					if(null != paymentObject.getF24flatRate()) {
						payObj = paymentObject.getF24flatRate().getRef();
						paymentKeys.put("f24flatRateKey", null != payObj.getKey() ? payObj.getKey() : null);
					}
					if(null != paymentObject.getF24standard()) {
						payObj = paymentObject.getF24standard().getRef();
						paymentKeys.put("f24standardKey", null != payObj.getKey() ? payObj.getKey() : null);
					}
				}
				paymentData.add(new PaymentDocumentData(recipient, paymentKeys));
			}
		}
		return paymentData;
	}
}
