package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.NotificationCsvBean;
import it.gov.pagopa.logextractor.dto.NotificationGeneralData;
import it.gov.pagopa.logextractor.dto.PaymentDocumentData;
import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.ResponseConstructor;
import it.gov.pagopa.logextractor.util.SortOrders;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchQueryConstructor;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchQuerydata;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchRangeQueryData;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchSortFilter;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchUtil;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;
import it.gov.pagopa.logextractor.util.external.selfcare.SelfCareApiHandler;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LogServiceImpl implements LogService{
	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	@Value("${external.denomination.ensureRecipientByExternalId.url}")
	String getUniqueIdURL;
	
	@Value("${external.denomination.getRecipientDenominationByInternalId.url}")
	String getTaxCodeURL;
	
	@Value("${external.opensearch.url}")
	String openSearchURL;
	
	@Value("${external.opensearch.basicauth.username}")
	String openSearchUsername;
	
	@Value("${external.opensearch.basicauth.password}")
	String openSearchPassword;
	
	@Value("${external.notification.getSentNotification.url}")
	String notificationURL;
	
	@Value("${external.selfcare.encodedIpaCode.url}")
	String selfCareEncodedIpaCodeURL;
	
	@Value("${external.notification.getLegalFactDownloadMetadata.url}")
	String legalFactDownloadMetadataURL;
	
	@Value("${external.notification.getNotificationAttachmentDownloadMetadata.url}")
	String notificationAttachmentDownloadMetadataURL;
	
	@Value("${external.notification.getPaymentAttachmentDownloadMetadata.url}")
	String paymentAttachmentDownloadMetadataURL;
	
	@Autowired
	NotificationApiHandler notificationApiHandler;
	
	@Autowired
	OpenSearchApiHandler openSearchApiHandler;
	
	@Autowired
	DeanonimizationApiHandler deanonimizationApiHandler;
	
	@Autowired
	SelfCareApiHandler selfCareApiHandler;

	@Override
	public DownloadArchiveResponseDto getAnonymizedPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun, String personId) throws IOException {
		log.info("Anonymized logs retrieve process - START - ticketNumber={}", ticketNumber);
		//TODO: Add audit trail log
		long millis = Instant.now().getEpochSecond();
		ArrayList<String> openSearchResponse = null;
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<OpenSearchQuerydata>();
		HashMap<String, Object> queryParams = new HashMap<String, Object>();
		String query = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && iun == null) {
			log.info("Getting activities' anonymized history, user={}, startDate={}, endDate={}", personId, dateFrom, dateTo);
			queryParams.put("uid", personId);
			queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
			log.info("Constructing Opensearch query...");
			query = queryConstructor.createBooleanMultiSearchQuery(queryData);
			log.info("Executing query:"+query);
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		} else {
			// use case 8
			if (iun != null && ticketNumber!=null) {
				log.info("Getting anonymized path, notification={}", iun);
				String legalStartDate = notificationApiHandler.getNotificationLegalStartDate(notificationURL, iun);	
				String dateIn3Months = OffsetDateTime.parse(legalStartDate).plusMonths(3).toString();
				queryParams.put("iun", iun);
				queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
						new OpenSearchRangeQueryData("@timestamp", legalStartDate, dateIn3Months), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
				log.info("Constructing Opensearch query...");
				query = queryConstructor.createBooleanMultiSearchQuery(queryData);
				log.info("Executing query:"+query);
				openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			}
		}
		log.info("Constructing response...");
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(openSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Anonymized logs retrieve process - END in {} milliseconds", Instant.now().getEpochSecond() - millis);
		return response;
	}

	@Override
	public DownloadArchiveResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode) throws IOException, ParseException,CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		log.info("Monthly notifications retrieve process - START - ticketNumber={}", ticketNumber);
		//TODO: Add audit trail log
		long millis = Instant.now().getEpochSecond();
		LocalDate startDate = LocalDate.parse(StringUtils.removeIgnoreCase(referenceMonth, "-")+"01", DateTimeFormatter.BASIC_ISO_DATE);
		LocalDate endDate = startDate.plusMonths(1);
		String finaldatePart = "T00:00:00.000Z";
		String encodedIpaCode = selfCareApiHandler.getEncodedIpaCode(selfCareEncodedIpaCodeURL, ipaCode);
		ArrayList<NotificationCsvBean> notifications = new ArrayList<NotificationCsvBean>();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("startDate", startDate.toString()+finaldatePart);
        parameters.put("endDate", endDate.toString()+finaldatePart);
        parameters.put("size", 100);
        log.info("Getting notifications general data, publicAuthority={}, startDate={}, endDate={}", encodedIpaCode, startDate, endDate);
		ArrayList<NotificationGeneralData> notificationsGeneralData = notificationApiHandler.getNotificationsByPeriod(notificationURL,
																		parameters, encodedIpaCode, 0, new ArrayList<NotificationGeneralData>());
		if(notificationsGeneralData != null) {
			log.info("Getting notifications' details");
			for(NotificationGeneralData nTemp : notificationsGeneralData) {
				String legalStartDate = notificationApiHandler.getNotificationLegalStartDate(notificationURL, nTemp.getIun());
				StringBuilder recipientsBuilder = new StringBuilder();
				for(String tempRecipient : nTemp.getRecipients()) {
					recipientsBuilder.append(tempRecipient + "-");
				}
				recipientsBuilder.deleteCharAt(recipientsBuilder.length()-1);
				NotificationCsvBean notification = NotificationCsvBean.builder()
													.iun(nTemp.getIun())
													.sendDate(nTemp.getSentAt())
													.attestationGenerationDate(legalStartDate)
													.subject(nTemp.getSubject())
													.taxIds(recipientsBuilder.toString())
													.build();
				recipientsBuilder.setLength(0);
				notifications.add(notification);
			}
		}
		log.info("Constructing response...");
		DownloadArchiveResponseDto response = ResponseConstructor.createCsvLogResponse(notifications, Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Monthly notifications retrieve process - END in {} milliseconds", Instant.now().getEpochSecond() - millis);
		return response;
	}
	
	@Override
	public DownloadArchiveResponseDto getTraceIdLogs(String dateFrom, String dateTo, String traceId) throws IOException {
		log.info("Anonymized logs retrieve process - START");
		//TODO: Add audit trail log
		long millis = Instant.now().getEpochSecond();
		ArrayList<String> openSearchResponse = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		//use case 10
		if (dateFrom != null && dateTo != null && traceId != null) {
			log.info("Getting anonymized logs, traceId={} startDate={}, endDate={}", traceId, dateFrom, dateTo);
			HashMap<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("root_trace_id", traceId);
			OpenSearchQuerydata queryData = queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC));
			ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
			listOfQueryData.add(queryData);
			log.info("Constructing Opensearch query...");
			String query = queryConstructor.createBooleanMultiSearchQuery(listOfQueryData);
			log.info("Executing query:"+query);
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		}
		log.info("Constructing response...");
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(openSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Anonymized logs retrieve process - END in {} milliseconds", Instant.now().getEpochSecond() - millis);
		return response;
	}
	
	@Override
	public DownloadArchiveResponseDto getNotificationInfoLogs(String iun) throws IOException {
		OpenSearchApiHandler openSearchHandler = new OpenSearchApiHandler();
        OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
        NotificationApiHandler notificationHandler = new NotificationApiHandler();
        ArrayList<String> openSearchResponse = null;
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("iun", iun);
        OpenSearchQuerydata queryData = queryConstructor.prepareQueryData("pn-logs", queryParams, null, null);
        ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
        listOfQueryData.add(queryData);
        String query = queryConstructor.createBooleanMultiSearchQuery(listOfQueryData);
        System.out.println("Query:\n" + query);
        
//        openSearchResponse = openSearchHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
        
        Map<String, String> legalFactIds = notificationHandler.getNotificationLegalFactIdsAndTimestamp(notificationURL, iun);
        String docIdx = notificationHandler.getDocumentId(notificationURL, iun);
        ArrayList<PaymentDocumentData> paymentData = notificationHandler.getNotificationPaymentKeys(notificationURL, iun);
        
        System.out.println(docIdx);
        System.out.println(paymentData);
        System.out.println(paymentData.size());
        System.out.println(paymentData.get(0).getPaymentKeys().size());
        System.out.println(paymentData.get(0).getPaymentKeys());
        
		FileUtilities utils = new FileUtilities();
        
		// send request to
		// '/delivery-push/{iun}/legal-facts/{legalFactType}/{legalFactId}'
		String urlForLegalFactDownload = notificationHandler.getLegalFactMetadata(legalFactDownloadMetadataURL, iun, legalFactIds.get("legalFactId"), legalFactIds.get("legalFactType"));
		
		System.out.println(urlForLegalFactDownload);
		
		byte[] legalFactByteArr = notificationHandler.getFile(urlForLegalFactDownload);
		File legalFactFile = utils.getFile(Constants.LEGAL_FACT_FILE_NAME, Constants.TXT_EXTENSION);
		FileUtils.writeByteArrayToFile(legalFactFile, legalFactByteArr);

		// send request to
		// /delivery/notifications/sent/{iun}/attachments/documents/{docIdx}
		String urlForNotificationDocument = notificationHandler
				.getNotificationDocuments(notificationAttachmentDownloadMetadataURL, iun, docIdx);
		
		byte[] notificationDocumentByteArr = notificationHandler.getFile(urlForNotificationDocument);
		File notificationDocumentFile = utils.getFile(Constants.NOTIFICAION_DOCUMENT_FILE_NAME, Constants.TXT_EXTENSION);
		FileUtils.writeByteArrayToFile(notificationDocumentFile, notificationDocumentByteArr);

		// send requests to
		// /delivery/notifications/sent/{iun}/attachments/payment/{recipientIdx}/{attachmentName}
		for (int recipients = 0; recipients < paymentData.size(); recipients++) {
			paymentData.get(recipients).getPaymentKeys().forEach((key, value) -> {
				if (value != null) {
					String urlForPaymentDocument = notificationHandler
							.getPaymentDocuments(paymentAttachmentDownloadMetadataURL, iun, value);
				}
			});
		}

        return null;
    
	}
		
	public DownloadArchiveResponseDto getDeanonymizedPersonLogs(RecipientTypes recipientType, String dateFrom, String dateTo, String ticketNumber, String taxid, String iun) throws IOException {
		log.info("Deanonymized logs retrieve process - START - ticketNumber={}", ticketNumber);
		//TODO: Add audit trail log
		long millis = Instant.now().getEpochSecond();
		ArrayList<String> openSearchResponse = null;
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<OpenSearchQuerydata>();
		HashMap<String, Object> queryParams = new HashMap<String, Object>();
		String query = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		ArrayList<String> deanonymizedOpenSearchResponse = new ArrayList<String>();
		String legalStartDate = notificationApiHandler.getNotificationLegalStartDate(notificationURL, iun);	
		String dateIn3Months = OffsetDateTime.parse(legalStartDate).plusMonths(3).toString();
		//use case 3
		if (dateFrom != null && dateTo != null && taxid != null && recipientType!=null && ticketNumber!=null && iun==null) {
			log.info("Getting activities' deanonymized history, user={}, startDate={}, endDate={}", taxid, dateFrom, dateTo);
			log.info("Calling deanonimization service...");
			GetBasicDataResponseDto internalidDto = deanonimizationApiHandler.getUniqueIdentifierForPerson(recipientType, taxid, getUniqueIdURL);
			log.info("Returned deanonimized data: " + internalidDto.toString());
			queryParams.put("uid", internalidDto.getData());
			queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
			log.info("Constructing Opensearch query...");
			query = queryConstructor.createBooleanMultiSearchQuery(queryData);
			log.info("Executing query:"+query);
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			log.info("Deanonymizing results...");
			deanonymizedOpenSearchResponse = OpenSearchUtil.toDeanonymizedDocuments(openSearchResponse, getTaxCodeURL, deanonimizationApiHandler);	
		} else{
			//use case 4
			if (iun!=null && ticketNumber!=null) {
				log.info("Getting deanonymized path, notification={}", iun);
				queryParams.put("iun", iun);
				queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
						new OpenSearchRangeQueryData("@timestamp", legalStartDate, dateIn3Months), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
				log.info("Constructing Opensearch query...");
				query = queryConstructor.createBooleanMultiSearchQuery(queryData);
				log.info("Executing query:"+query);
				openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
				log.info("Deanonymizing results...");
				deanonymizedOpenSearchResponse = OpenSearchUtil.toDeanonymizedDocuments(openSearchResponse, getTaxCodeURL, deanonimizationApiHandler);
			}
		}
		log.info("Constructing response...");
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(deanonymizedOpenSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Deanonymized logs retrieve process - END in {} milliseconds", Instant.now().getEpochSecond() - millis);
		return response;
	}
}
