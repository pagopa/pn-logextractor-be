package it.gov.pagopa.logextractor.util.external.pnservices;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gov.pagopa.logextractor.util.constant.ExternalServiceConstants;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
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
import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.dto.response.FileDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationHistoryResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationsGeneralDataResponseDto;

/**
 * Uility class for integrations with Piattaforma Notifiche notifcations related services
 * */
@Component
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
	 * @return The list of {@link NotificationData} notifications' general data
	 */
	public List<NotificationData> getNotificationsByMonthsPeriod(OffsetDateTime referenceMonth, OffsetDateTime endMonth,
			String encodedPublicAuthorityName) {
		return getNotificationsBetweenMonths(referenceMonth, endMonth, encodedPublicAuthorityName,
				new ArrayList<>(), null);
	}
	
	/**
	 * Recursively performs a GET HTTP request to the PN external service to retrieve the
	 * general data of the notifications managed within a month
	 * @param referenceMonth The month to obtain the notifications for
	 * @param endMonth The following month of the reference month
	 * @param encodedPublicAuthorityName   The public authority id
	 * @param notifications The initial notifications list
	 * @param nextUrlKey The key of the next results page
	 * @return The list of {@link NotificationData} notifications' general data
	 */
	private ArrayList<NotificationData> getNotificationsBetweenMonths(OffsetDateTime referenceMonth, OffsetDateTime endMonth, String encodedPublicAuthorityName,
			ArrayList<NotificationData> notifications, String nextUrlKey) {
		HttpHeaders requestHeaders = new HttpHeaders();
	    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	    List<MediaType> acceptedTypes = new ArrayList<>();
	    acceptedTypes.add(MediaType.APPLICATION_JSON);
	    requestHeaders.setAccept(acceptedTypes);
	    HttpEntity<?> entity = new HttpEntity<>(requestHeaders);
	    String urlTemplate = null == nextUrlKey ? 
	    		UriComponentsBuilder.fromHttpUrl(notificationURL)
	    		.queryParam(ExternalServiceConstants.EXT_SENDER_ID_PARAM, "{senderId}")
		        .queryParam(ExternalServiceConstants.EXT_START_DATE_PARAM, "{startDate}")
		        .queryParam(ExternalServiceConstants.EXT_END_DATE_PARAM, "{endDate}")
		        .queryParam(ExternalServiceConstants.EXT_SIZE_PARAM, "{size}")
		        .encode()
		        .toUriString() 
		        :
		        UriComponentsBuilder.fromHttpUrl(notificationURL)
		        .queryParam(ExternalServiceConstants.EXT_SENDER_ID_PARAM, "{senderId}")
    			.queryParam(ExternalServiceConstants.EXT_START_DATE_PARAM, "{startDate}")
		        .queryParam(ExternalServiceConstants.EXT_END_DATE_PARAM, "{endDate}")
		        .queryParam(ExternalServiceConstants.EXT_SIZE_PARAM, "{size}")
		        .queryParam(ExternalServiceConstants.EXT_NEXT_PAGE_KEY_PARAM, "{nextPagesKey}")
		        .encode()
		        .toUriString();
	    HashMap<String, Object> params = new HashMap<>();
	    params.put(ExternalServiceConstants.EXT_SENDER_ID_PARAM, encodedPublicAuthorityName);
    	params.put(ExternalServiceConstants.EXT_START_DATE_PARAM, referenceMonth);
    	params.put(ExternalServiceConstants.EXT_END_DATE_PARAM, endMonth);
	    params.put(ExternalServiceConstants.EXT_SIZE_PARAM, GenericConstants.PAGE_SIZE);
	    if(null != nextUrlKey) {
	    	params.put(ExternalServiceConstants.EXT_NEXT_PAGE_KEY_PARAM, nextUrlKey);
	    }
	    NotificationsGeneralDataResponseDto response = client.exchange(
	    		urlTemplate, 
				HttpMethod.GET,
				entity,
				NotificationsGeneralDataResponseDto.class,
				params).getBody();
	    if(response != null && (null == response.getNextPagesKey() || response.getNextPagesKey().isEmpty()) && Boolean.FALSE.equals(response.getMoreResult())) {
	    	return response.getResultsPage();
	    }
	    if(response != null) {
	    	notifications.addAll(response.getResultsPage());
	    	for(String currentKey : response.getNextPagesKey()) {
				notifications.addAll(getNotificationsBetweenMonths(referenceMonth, endMonth, 
						encodedPublicAuthorityName, notifications, currentKey));
		    }
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
	 * @return A {@link FileDownloadMetadataResponseDto} representing the download metadata
	 */
	public FileDownloadMetadataResponseDto getDownloadMetadata(String key) {
		String url = String.format(downloadFileURL, safeStorageEndpoint, safeStorageStage, key);
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_PDF);
		requestHeaders.set("x-pagopa-safestorage-cx-id", safeStorageCxid);
		List<MediaType> acceptedTypes = new ArrayList<>();
		acceptedTypes.add(MediaType.APPLICATION_PDF);
		requestHeaders.setAccept(acceptedTypes);
		HttpEntity<?> entity = new HttpEntity<>(requestHeaders);
		return client.exchange(url, HttpMethod.GET, entity, FileDownloadMetadataResponseDto.class).getBody();
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
	 * Performs a GET HTTP request to get the notification history
	 * @param iun The notification IUN
	 * @param numberOfRecipients The number of recipients associated to the notification
	 * @param createdAt The notification creation date
	 * @return A {@link NotificationHistoryResponseDto} representing the notification history
	 * */
	public NotificationHistoryResponseDto getNotificationHistory(String iun, int numberOfRecipients, String createdAt) {
		String url = String.format(notificationHistoryURL, iun);
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
		        .queryParam(ExternalServiceConstants.EXT_NUM_RECIPIENTS_PARAM, "{numberOfRecipients}")
		        .queryParam(ExternalServiceConstants.EXT_CREATED_AT_PARAM, "{createdAt}")
		        .encode()
		        .toUriString();
		Map<String, Object> params = new HashMap<>();
		params.put(ExternalServiceConstants.EXT_NUM_RECIPIENTS_PARAM, numberOfRecipients);
		params.put(ExternalServiceConstants.EXT_CREATED_AT_PARAM, createdAt);
		return client.exchange(urlTemplate, HttpMethod.GET, entity, NotificationHistoryResponseDto.class, params).getBody();
	}
	
	/**
	 * Extracts the legal fact documents' keys from the input notification history
	 * @param notificationInfo The notification details
	 * @return The keys list
	 * */
	public List<String> getLegalFactKeys(NotificationHistoryResponseDto notificationInfo) {
		ArrayList<String> legalFactKeys = new ArrayList<>();
		if(null != notificationInfo.getTimeline()) {
			for (NotificationDetailsTimelineData timelineObject : notificationInfo.getTimeline()) {
				if (null != timelineObject.getLegalFactsIds()) {
					for(NotificationDetailsTimelineLegalFactsData legalFactsObject : timelineObject.getLegalFactsIds()) {
						legalFactKeys.add(StringUtils.remove(legalFactsObject.getKey(), GenericConstants.SAFESTORAGE_PREFIX));
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
	public List<String> getDocumentKeys(NotificationDetailsResponseDto notificationInfo) {
		ArrayList<String> docIdxs = new ArrayList<>();
		if(null != notificationInfo.getDocuments()) {
			for (NotificationDetailsDocumentData doc : notificationInfo.getDocuments()) {
				docIdxs.add(StringUtils.remove(doc.getRef().getKey(), GenericConstants.SAFESTORAGE_PREFIX));
			}
		}
		return docIdxs;
	}
	
	/**
	 * Extracts the payment documents' keys from the input notification details
	 * @param notificationInfo The notification details
	 * @return The keys list
	 * */
	public List<String> getPaymentKeys(NotificationDetailsResponseDto notificationInfo) {
		ArrayList<String> paymentKeys = new ArrayList<>();
		if(null != notificationInfo && null != notificationInfo.getRecipients()) {
			for(NotificationDetailsRecipientsData recipient : notificationInfo.getRecipients()) {
				paymentKeys.addAll(getCurrentRecipientPaymentKeys(recipient));
			}
		}
		return paymentKeys;
	}

	private List<String> getCurrentRecipientPaymentKeys(NotificationDetailsRecipientsData recipient) {
		ArrayList<String> currentRecipientPaymentKeys = new ArrayList<>();
		if(null != recipient.getPayment()) {
			if(null != recipient.getPayment().getF24flatRate()) {
				currentRecipientPaymentKeys.add(StringUtils.remove(recipient.getPayment().getF24flatRate().getRef().getKey(), GenericConstants.SAFESTORAGE_PREFIX));
			}
			if(null != recipient.getPayment().getF24standard()) {
				currentRecipientPaymentKeys.add(StringUtils.remove(recipient.getPayment().getF24standard().getRef().getKey(), GenericConstants.SAFESTORAGE_PREFIX));
			}
			if(null != recipient.getPayment().getPagoPaForm()) {
				currentRecipientPaymentKeys.add(StringUtils.remove(recipient.getPayment().getPagoPaForm().getRef().getKey(), GenericConstants.SAFESTORAGE_PREFIX));
			}
		}
		return currentRecipientPaymentKeys;
	}
}
