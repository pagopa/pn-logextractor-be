package it.gov.pagopa.logextractor.util.external.pnservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import it.gov.pagopa.logextractor.dto.NotificationGeneralData;
import it.gov.pagopa.logextractor.dto.PaymentDocumentData;
import it.gov.pagopa.logextractor.dto.response.LegalFactDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationAttachmentDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.util.JsonUtilities;

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
			String encodedIpaCode, ArrayList<NotificationGeneralData> notifications, String nextUrlKey, JSONArray pages) {
		HttpHeaders requestHeaders = new HttpHeaders();
	    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	    requestHeaders.set("x-ipa-code", encodedIpaCode);
	    List<MediaType> acceptedTypes = new ArrayList<MediaType>();
	    acceptedTypes.add(MediaType.APPLICATION_JSON);
	    requestHeaders.setAccept(acceptedTypes);
	    HashMap<String, Object> parameters = new HashMap<String, Object>();
	    for (Map.Entry<String, Object> entry : params.entrySet()) {
	    	parameters.put(entry.getKey(), entry.getValue());
	    }
	    ResponseEntity<String> response = client.getForEntity(url, String.class, parameters);
	    JSONObject responseObj = new JSONObject(response.getBody());
	    if(responseObj.isNull("nextPagesKey") || !responseObj.getBoolean("moreResult")) {
	    	return getNotificationsGeneralData(response.getBody());
	    }
	    JSONArray pageKeys = JsonUtilities.getArray(response.getBody(), "nextPagesKey");
	    notifications.addAll(getNotificationsGeneralData(response.getBody()));
	    int keySize = pageKeys.length();
	    for(int index = 0; index < keySize; index++) {
	    	String nextKey = pageKeys.getString(index);
	    	HashMap<String, Object> newParameters = new HashMap<String, Object>();
		    newParameters.putAll(parameters);
			newParameters.put("nextPagesKey", nextKey);
			notifications.addAll(getNotificationsByPeriod(url, newParameters, encodedIpaCode, notifications, nextKey, pageKeys));
	    }
	    return notifications;
	}
	/*public ArrayList<NotificationGeneralData> getNotificationsByPeriod(String url, HashMap<String, Object> params, 
				String encodedIpaCode, int currentKey, ArrayList<NotificationGeneralData> notifications) {
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("x-ipa-code", encodedIpaCode);
        List<MediaType> acceptedTypes = new ArrayList<MediaType>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
        	parameters.put(entry.getKey(), entry.getValue());
        }
        ResponseEntity<String> response = client.getForEntity(url, String.class, parameters);
        JSONObject responseObj = new JSONObject(response.getBody());
        if(responseObj.isNull("nextPagesKey")) {
        	return notifications;
        }
        JSONArray pageKeys = JsonUtilities.getArray(response.getBody(), "nextPagesKey");
        notifications.addAll(getNotificationsGeneralData(response.getBody()));
        if(null == pageKeys || pageKeys.length() == 0 || currentKey == pageKeys.length()) {
        	return notifications;
        }
    	HashMap<String, Object> newParameters = new HashMap<String, Object>();
    	newParameters.put("nextPagesKey", pageKeys.get(currentKey));
        return getNotificationsByPeriod(url, newParameters, encodedIpaCode, currentKey+1, notifications);
	}*/
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve a
	 * notification legal start date
	 * 
	 * @param url The PN external service base URL
	 * @param iun The notification IUN
	 * @return The notification legal start date
	 */
	public String getNotificationDetails(String externalServiceUrl, String iun) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> acceptedTypes = new ArrayList<MediaType>();
		acceptedTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptedTypes);
		return client.getForEntity(externalServiceUrl + "/" + iun, String.class).getBody();
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
	private ArrayList<NotificationGeneralData> getNotificationsGeneralData(String notificationResponse) {
		ArrayList<NotificationGeneralData> notificationsGeneralData = new ArrayList<NotificationGeneralData>();
		JSONObject responseObj = new JSONObject(notificationResponse);
        if(responseObj.isNull("resultsPage")) {
        	return notificationsGeneralData;
        }
		JSONArray timelineObjectsArray = new JSONObject(notificationResponse).getJSONArray("resultsPage");
        for(int index = 0; index < timelineObjectsArray.length(); index++) {
        	JSONArray recipients = timelineObjectsArray.getJSONObject(index).getJSONArray("recipients");
        	ArrayList<String> recipientsList = new ArrayList<String>();
        	for(int indexRecipient = 0; indexRecipient < recipients.length(); indexRecipient++) {
        		recipientsList.add(recipients.getString(indexRecipient));
        	}
        	NotificationGeneralData currentNotificationData = new NotificationGeneralData();
        	currentNotificationData.setIun(timelineObjectsArray.getJSONObject(index).getString("iun"));
        	currentNotificationData.setSentAt(timelineObjectsArray.getJSONObject(index).getString("sentAt"));
        	currentNotificationData.setSubject(timelineObjectsArray.getJSONObject(index).getString("subject"));
        	currentNotificationData.setRecipients(recipientsList);
        	notificationsGeneralData.add(currentNotificationData);
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
	public String getLegalStartDate(String notificationInfo) {
		String legalStartDate = null;
		JSONArray timelineObjectsArray = new JSONObject(notificationInfo).getJSONArray("timeline");
        for(int index = 0; index < timelineObjectsArray.length(); index++) {
        	JSONObject timelineObject = timelineObjectsArray.getJSONObject(index);
        	if("REQUEST_ACCEPTED".equalsIgnoreCase(timelineObject.getString("category"))) {
        		legalStartDate = timelineObject.getString("timestamp");
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
	public Map<String, String> getLegalFactIdsAndTimestamp(String notificationInfo) {
		Map<String, String> legalFactIds = new HashMap<>();
		JSONArray timelineObjectsArray = new JSONObject(notificationInfo).getJSONArray("timeline");
		for (int index = 0; index < timelineObjectsArray.length(); index++) {
			JSONObject timelineObject = timelineObjectsArray.getJSONObject(index);
			if ("REQUEST_ACCEPTED".equalsIgnoreCase(timelineObject.getString("category"))) {
				legalFactIds.put("timestamp", timelineObject.getString("timestamp"));
				JSONArray legalFactIdsArray = timelineObjectsArray.getJSONObject(index).getJSONArray("legalFactsIds");
				for (int indexFactsIds = 0; indexFactsIds < legalFactIdsArray.length(); indexFactsIds++) {
					JSONObject legalFactsObject = legalFactIdsArray.getJSONObject(indexFactsIds);
					legalFactIds.put("legalFactId", legalFactsObject.getString("key"));
					legalFactIds.put("legalFactType", legalFactsObject.getString("category"));
				}
			}
		}
		return legalFactIds;
	}
	
	/**
	 * Method that retrieves the doc id of the documents that are attached to the
	 * notification
	 * 
	 * @param notificationInfo The PN external service response containing the
	 *                         notification details
	 * @return A list containing the document ids
	 */
	public ArrayList<String> getDocumentIds(String notificationInfo) {
		ArrayList<String> docIdxs = new ArrayList<String>();
		JSONArray documentObjectsArray = new JSONObject(notificationInfo).getJSONArray("documents");
		for (int index = 0; index < documentObjectsArray.length(); index++) {
			docIdxs.add(documentObjectsArray.getJSONObject(index).getString("docIdx"));
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
	public ArrayList<PaymentDocumentData> getPaymentKeys(String notificationInfo) {
		ArrayList<PaymentDocumentData> paymentData = new ArrayList<PaymentDocumentData>();
		Map<String, String> paymentKeys = new HashMap<>();
		JSONObject payObj = null;
		JSONObject paymentObject = null;
		JSONObject notificationDetails = new JSONObject(notificationInfo);
		JSONArray recipientsObjectsArray = new JSONArray();
		if(notificationDetails.isNull("recipients")) {
			return paymentData;
		}
		recipientsObjectsArray = notificationDetails.getJSONArray("recipients");
		for (int recipient = 0; recipient < recipientsObjectsArray.length(); recipient++) {
			if(!recipientsObjectsArray.getJSONObject(recipient).isNull("payment")) {
				paymentObject = recipientsObjectsArray.getJSONObject(recipient).getJSONObject("payment");
				if(!paymentObject.isNull("pagoPaForm")) {
					payObj = paymentObject.getJSONObject("pagoPaForm").getJSONObject("ref");
					paymentKeys.put("pagoPaFormKey", !payObj.isNull("key") ? payObj.getString("key") : null);
				}
				if(!paymentObject.isNull("f24flatRate")) {
					payObj = paymentObject.getJSONObject("f24flatRate").getJSONObject("ref");
					paymentKeys.put("f24flatRateKey", !payObj.isNull("key") ? payObj.getString("key") : null);
				}
				if(!paymentObject.isNull("f24standard")) {
					payObj = paymentObject.getJSONObject("f24standard").getJSONObject("ref");
					paymentKeys.put("f24standardKey", !payObj.isNull("key") ? payObj.getString("key") : null);
				}
			}
			paymentData.add(new PaymentDocumentData(recipient, paymentKeys));
		}
		return paymentData;
	}
}
