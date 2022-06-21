package it.gov.pagopa.logextractor.util.external.pnservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import it.gov.pagopa.logextractor.config.ApplicationContextProvider;
import it.gov.pagopa.logextractor.dto.NotificationGeneralData;

@Component
public class NotificationApiHandler {
	
	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the general data of the notifications managed within a period
	 * @param url The PN external service base URL
	 * @param startDate The period start date
	 * @param endDate The period end date
	 * @param size The maximum number of results to be retrieved
	 * @return The list of notifications' general data
	 * */
	public ArrayList<NotificationGeneralData> getNotificationsByPeriod(String url, String encodedIpaCode, String startDate, String endDate, int size) {	
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("x-ipa-code", encodedIpaCode);
        List<MediaType> acceptedTypes = new ArrayList<MediaType>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("startDate", startDate);
        parameters.put("endDate", endDate);
        parameters.put("size", size);
        ResponseEntity<String> response = client.getForEntity(url, String.class, parameters);
        return getNotificationsGeneralData(response.getBody());
	}
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve a notification legal start date
	 * @param url The PN external service base URL
	 * @param iun The notification IUN
	 * @return The notification legal start date
	 * */
	public String getNotificationLegalStartDate(String url, String iun) {
		HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> acceptedTypes = new ArrayList<MediaType>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        ResponseEntity<String> response = client.getForEntity(url+"/"+iun, String.class);
        return getLegalStartDate(response.getBody());
	}
	
	/**
	 * Gets the general data of a notification
	 * @param notificationResponse The PN external service response containing the notification general data
	 * @return A list containing all the notifications' general data
	 * */
	private ArrayList<NotificationGeneralData> getNotificationsGeneralData(String notificationResponse) {
		ArrayList<NotificationGeneralData> notificationsGeneralData = new ArrayList<NotificationGeneralData>();
		JSONArray timelineObjectsArray = new JSONObject(notificationResponse).getJSONArray("resultsPage");
        for(int index = 0; index < timelineObjectsArray.length(); index++) {
        	JSONArray recipients = timelineObjectsArray.getJSONObject(index).getJSONArray("recipients");
        	ArrayList<String> recipientsList = new ArrayList<String>();
        	for(int indexRecipient = 0; indexRecipient < recipients.length(); indexRecipient++) {
        		recipientsList.add(recipients.getString(indexRecipient));
        	}
        	NotificationGeneralData currentNotificationData = NotificationGeneralData.builder()
        														.iun(timelineObjectsArray.getJSONObject(index).getString("iun"))
        														.sentAt(timelineObjectsArray.getJSONObject(index).getString("sentAt"))
        														.subject(timelineObjectsArray.getJSONObject(index).getString("subject"))
        														.recipients(recipientsList)
        														.build();
        	notificationsGeneralData.add(currentNotificationData);
        }
        return notificationsGeneralData;
	}
	
	/**
	 * Gets the legal start date of a notification
	 * @param notificationInfo The PN external service response containing the notification details
	 * @return The notification legal start date
	 * */
	private String getLegalStartDate(String notificationInfo) {
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
}
