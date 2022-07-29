package it.gov.pagopa.logextractor.util.external.pnservices;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import it.gov.pagopa.logextractor.dto.NotificationDetailsDocumentData;
import it.gov.pagopa.logextractor.dto.NotificationDetailsRecipientsData;
import it.gov.pagopa.logextractor.dto.NotificationDetailsTimelineData;
import it.gov.pagopa.logextractor.dto.NotificationDetailsTimelineLegalFactsData;
import it.gov.pagopa.logextractor.dto.NotificationGeneralData;
import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.dto.response.FileDownloadMetadataResponseDTO;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationHistoryResponseDTO;
import it.gov.pagopa.logextractor.dto.response.NotificationsGeneralDataResponseDto;
import lombok.extern.slf4j.Slf4j;

/**
 * Uility class for integrations with Piattaforma Notifiche notifcations related services
 * */
@Component
@Slf4j
public class NotificationApiHandler {
	
	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	@Value("${external.notification.getNotificationHistory.url}")
	String notificationHistoryURL;
	
	@Value("${external.safeStorage.downloadFile.url}")
	String downloadFileURL;
	
	@Value("${external.safeStorage.downloadFile.endpoint}")
	String safeStorageEndpoint;
	
	@Value("${external.safeStorage.downloadFile.stage}")
	String safeStorageStage;
	
	@Value("${external.notification.getSentNotification.url}")
	String notificationURL;
	
	@Value("${external.notification.getSentNotificationDetails.url}")
	String notificationDetailsURL;
	
	@Value("${external.safeStorage.downloadFile.cxId}")
	String safeStorageCxid;
	
	/**
	 * Invokes a utility method to get the notifications managed by a public authority within a month
	 * @param referenceMonth The month to obtain the notifications for
	 * @param endMonth The following month of the reference month
	 * @param encodedPublicAuthorityName   The public authority id
	 * @param userIdentifier The user unique identifier
	 * @return The list of {@link NotificationGeneralData} notifications' general data
	 */
	public ArrayList<NotificationGeneralData> getNotificationsByMonthsPeriod(String referenceMonth, String endMonth, 
			String encodedPublicAuthorityName, String userIdentifier) {
		return getNotificationsBetweenMonths(referenceMonth, endMonth, encodedPublicAuthorityName,
				new ArrayList<NotificationGeneralData>(), null, userIdentifier);
	}
	
	/**
	 * Recursively performs a GET HTTP request to the PN external service to retrieve the
	 * general data of the notifications managed within a month
	 * @param referenceMonth The month to obtain the notifications for
	 * @param endMonth The following month of the reference month
	 * @param encodedPublicAuthorityName   The public authority id
	 * @param notifications The initial notifications list
	 * @param nextUrlKey The key of the next results page
	 * @param userIdentifier The user unique identifier
	 * @return The list of {@link NotificationGeneralData} notifications' general data
	 */
	private ArrayList<NotificationGeneralData> getNotificationsBetweenMonths(String referenceMonth, String endMonth, String encodedPublicAuthorityName, 
			ArrayList<NotificationGeneralData> notifications, String nextUrlKey, String userIdentifier) {
		HttpHeaders requestHeaders = new HttpHeaders();
	    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	    requestHeaders.set("x-pagopa-pn-cx-id", encodedPublicAuthorityName);
	    requestHeaders.set("x-pagopa-pn-uid", "HD-"+userIdentifier);
	    List<MediaType> acceptedTypes = new ArrayList<MediaType>();
	    acceptedTypes.add(MediaType.APPLICATION_JSON);
	    requestHeaders.setAccept(acceptedTypes);
	    HttpEntity<?> entity = new HttpEntity<>(requestHeaders);
	    String urlTemplate = null == nextUrlKey ? 
	    		UriComponentsBuilder.fromHttpUrl(notificationURL)
		        .queryParam("startDate", "{startDate}")
		        .queryParam("endDate", "{endDate}")
		        .queryParam("size", "{size}")
		        .encode()
		        .toUriString() 
		        :
		        UriComponentsBuilder.fromHttpUrl(notificationURL)
    			.queryParam("startDate", "{startDate}")
		        .queryParam("endDate", "{endDate}")
		        .queryParam("size", "{size}")
		        .queryParam("nextPagesKey", "{nextPagesKey}")
		        .encode()
		        .toUriString();
	    HashMap<String, Object> params = new HashMap<String, Object>();
	    if(null != referenceMonth) {
	    	params.put("startDate", referenceMonth);
	    }
	    if(null != endMonth) {
	    	params.put("endDate", endMonth);
	    }
	    if(null != nextUrlKey) {
	    	params.put("nextPagesKey", nextUrlKey);
	    }
	    params.put("size", 1000);
	    NotificationsGeneralDataResponseDto response = client.exchange(
	    		urlTemplate, 
				HttpMethod.GET,
				entity,
				NotificationsGeneralDataResponseDto.class,
				params).getBody();
	    if((null == response.getNextPagesKey() || response.getNextPagesKey().size() == 0) && !response.getMoreResult()) {
	    	return getNotificationsGeneralData(response);
	    }
	    notifications.addAll(getNotificationsGeneralData(response));
	    for(String currentKey : response.getNextPagesKey()) {
			notifications.addAll(getNotificationsBetweenMonths(referenceMonth, endMonth, 
					encodedPublicAuthorityName, notifications, currentKey, userIdentifier));
	    }
	    return notifications;
	}
	
	/**
	 * Performs a GET HTTP request to get the notification details
	 * @param iun The notification IUN
	 * @return A {@link NotificationDetailsResponseDto} representing the notification details
	 */
	public NotificationDetailsResponseDto getNotificationDetails(String iun) {
		String url = String.format(notificationDetailsURL, iun);
        return client.getForEntity(url, NotificationDetailsResponseDto.class).getBody();
	}

	/**
	 * Performs a GET HTTP request to obtain the download metadata associated with the input document key
	 * @param key The document key
	 * @return A {@link FileDownloadMetadataResponseDTO} representing the download metadata
	 */
	public FileDownloadMetadataResponseDTO getDownloadMetadata(String key) {
		String url = String.format(downloadFileURL, safeStorageEndpoint, safeStorageStage, key);
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_PDF);
		requestHeaders.set("x-pagopa-safestorage-cx-id", safeStorageCxid);
		List<MediaType> acceptedTypes = new ArrayList<MediaType>();
		acceptedTypes.add(MediaType.APPLICATION_PDF);
		requestHeaders.setAccept(acceptedTypes);
		HttpEntity<?> entity = new HttpEntity<>(requestHeaders);
		return client.exchange(url, HttpMethod.GET, entity, FileDownloadMetadataResponseDTO.class).getBody();
	}
	
	/**
	 * Performs a GET HTTP request to obtain a physical file represented as a byte array
	 * @param url The URL to make the request to
	 * @return A byte array representation of a file
	 * @throws IOException 
	 */
	public byte[] getFile(String url) throws IOException {
		try {
			URL urlToFileDownload = new URL(url);
            return IOUtils.toByteArray(urlToFileDownload);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly();
        }
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
	 * Performs a GET HTTP request to get the notification history
	 * @param iun The notification IUN
	 * @param numberOfRecipients The nnumber of recipients associated to the notification
	 * @param createdAt The notification creation date
	 * @return A {@link NotificationHistoryResponseDTO} representing the notification history
	 * */
	public NotificationHistoryResponseDTO getNotificationHistory(String iun, int numberOfRecipients, String createdAt) {
		String url = String.format(notificationHistoryURL, iun);
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
		        .queryParam("numberOfRecipients", "{numberOfRecipients}")
		        .queryParam("createdAt", "{createdAt}")
		        .encode()
		        .toUriString();
		Map<String, Object> params = new HashMap<>();
		params.put("numberOfRecipients", numberOfRecipients);
		params.put("createdAt", createdAt);
		return client.exchange(urlTemplate, HttpMethod.GET, entity, NotificationHistoryResponseDTO.class, params).getBody();
	}
	
	/**
	 * Extracts the legal fact documents' keys from the input notification history
	 * @param notificationInfo The notification details
	 * @return The keys list
	 * */
	public ArrayList<String> getLegalFactKeys(NotificationHistoryResponseDTO notificationInfo) {
		ArrayList<String> legalFactKeys = new ArrayList<String>();
		if(null != notificationInfo.getTimeline()) {
			for (NotificationDetailsTimelineData timelineObject : notificationInfo.getTimeline()) {
				if (null != timelineObject.getLegalFactsIds()) {
					for(NotificationDetailsTimelineLegalFactsData legalFactsObject : timelineObject.getLegalFactsIds()) {
						legalFactKeys.add(StringUtils.remove(legalFactsObject.getKey(), "safestorage://"));
					}
				}
			}
		}
		return legalFactKeys;
	}
	
	/**
	 * Extracts the documents' keys from the input notification details
	 * @param notificationInfo The notification details
	 * @return The keys list
	 * */
	public ArrayList<String> getDocumentKeys(NotificationDetailsResponseDto notificationInfo) {
		ArrayList<String> docIdxs = new ArrayList<String>();
		if(null != notificationInfo.getDocuments()) {
			for (NotificationDetailsDocumentData doc : notificationInfo.getDocuments()) {
				docIdxs.add(StringUtils.remove(doc.getRef().getKey(), "safestorage://"));
			}
		}
		return docIdxs;
	}
	
	/**
	 * Extracts the payment documents' keys from the input notification details
	 * @param notificationInfo The notification details
	 * @return The keys list
	 * */
	public ArrayList<String> getPaymentKeys(NotificationDetailsResponseDto notificationInfo) {
		ArrayList<String> paymentKeys = new ArrayList<String>();
		if(null != notificationInfo.getRecipients()) {
			for(NotificationDetailsRecipientsData recipient : notificationInfo.getRecipients()) {
				if(null != recipient.getPayment()) {
					if(null != recipient.getPayment().getF24flatRate()) {
						paymentKeys.add(StringUtils.remove(recipient.getPayment().getF24flatRate().getRef().getKey(), "safestorage://"));
					}
					if(null != recipient.getPayment().getF24standard()) {
						paymentKeys.add(StringUtils.remove(recipient.getPayment().getF24standard().getRef().getKey(), "safestorage://"));
					}
					if(null != recipient.getPayment().getPagoPaForm()) {
						paymentKeys.add(StringUtils.remove(recipient.getPayment().getPagoPaForm().getRef().getKey(), "safestorage://"));
					}
				}
				
			}
		}
		return paymentKeys;
	}
}
