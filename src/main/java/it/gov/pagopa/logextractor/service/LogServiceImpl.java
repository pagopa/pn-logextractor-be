package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
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
import it.gov.pagopa.logextractor.dto.response.LegalFactDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationAttachmentDownloadMetadataResponseDto;
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
		log.info("Anonymized logs retrieve process - START - user={}, ticket number={}", MDC.get("user_identifier"), ticketNumber);
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
		ArrayList<String> openSearchResponse = null;
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<OpenSearchQuerydata>();
		HashMap<String, Object> queryParams = new HashMap<String, Object>();
		String query = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && iun == null) {
			log.info("Getting activities' anonymized history, user={}, startDate={}, endDate={}, constructing Opensearch query...", personId, dateFrom, dateTo);
			queryParams.put("uid", personId);
			queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
			query = queryConstructor.createBooleanMultiSearchQuery(queryData);
			log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
			performanceMillis = System.currentTimeMillis();
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		} else {
			// use case 8
			if (iun != null && ticketNumber!=null) {
				log.info("Getting anonymized path, notification={}, constructing Opensearch query...", iun);
				String notificationDetails = notificationApiHandler.getNotificationDetails(notificationURL, iun);	
				String legalStartDate = notificationApiHandler.getLegalStartDate(notificationDetails);
				String dateIn3Months = OffsetDateTime.parse(legalStartDate).plusMonths(3).toString();
				queryParams.put("iun.keyword", iun);
				queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
						new OpenSearchRangeQueryData("@timestamp", legalStartDate, dateIn3Months), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
				query = queryConstructor.createBooleanMultiSearchQuery(queryData);
				log.info("Executing query:" + RegExUtils.removeAll(query, "\n"));
				performanceMillis = System.currentTimeMillis();
				openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			}
		}
		log.info("Query execution completed in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(openSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Anonymized logs retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
		return response;
	}

	@Override
	public DownloadArchiveResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode) throws IOException, ParseException,CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		log.info("Monthly notifications retrieve process - START - user={}, ticket number={}", MDC.get("user_identifier"), ticketNumber);
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
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
        performanceMillis = System.currentTimeMillis();
		ArrayList<NotificationGeneralData> notificationsGeneralData = notificationApiHandler.getNotificationsByPeriod(notificationURL,
																		parameters, encodedIpaCode, 0, new ArrayList<NotificationGeneralData>());
		if(notificationsGeneralData != null) {
			log.info("Notifications general data retrieved in {} ms, getting notifications' details", System.currentTimeMillis() - performanceMillis);
			performanceMillis = System.currentTimeMillis();
			for(NotificationGeneralData nTemp : notificationsGeneralData) {
				String notificationDetails = notificationApiHandler.getNotificationDetails(notificationURL, nTemp.getIun());
				StringBuilder recipientsBuilder = new StringBuilder();
				for(String tempRecipient : nTemp.getRecipients()) {
					recipientsBuilder.append(tempRecipient + "-");
				}
				recipientsBuilder.deleteCharAt(recipientsBuilder.length()-1);
				NotificationCsvBean notification = NotificationCsvBean.builder()
													.iun(nTemp.getIun())
													.sendDate(nTemp.getSentAt())
													.attestationGenerationDate(notificationApiHandler.getLegalStartDate(notificationDetails))
													.subject(nTemp.getSubject())
													.taxIds(recipientsBuilder.toString())
													.build();
				recipientsBuilder.setLength(0);
				notifications.add(notification);
			}
		}
		log.info("Notification details recovered in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
		DownloadArchiveResponseDto response = ResponseConstructor.createCsvLogResponse(notifications, Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Monthly notifications retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
		return response;
	}
	
	@Override
	public DownloadArchiveResponseDto getTraceIdLogs(String dateFrom, String dateTo, String traceId) throws IOException {
		log.info("Anonymized logs retrieve process - START - user={}", MDC.get("user_identifier"));
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
		ArrayList<String> openSearchResponse = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		//use case 10
		if (dateFrom != null && dateTo != null && traceId != null) {
			log.info("Getting anonymized logs, traceId={} startDate={}, endDate={}, constructing Opensearch query...", traceId, dateFrom, dateTo);
			HashMap<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("root_trace_id", traceId);
			OpenSearchQuerydata queryData = queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC));
			ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
			listOfQueryData.add(queryData);
			String query = queryConstructor.createBooleanMultiSearchQuery(listOfQueryData);
			log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
			performanceMillis = System.currentTimeMillis();
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		}
		log.info("Query execution completed in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(openSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Anonymized logs retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
		return response;
	}
	
	@Override
	public DownloadArchiveResponseDto getNotificationInfoLogs(String ticketNumber, String iun) throws IOException, InterruptedException {
		log.info("Notification data retrieve process - START - user={}, ticket number={}", MDC.get("user_identifier"), ticketNumber);
		long serviceStartTime = System.currentTimeMillis();
        OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
        ArrayList<String> openSearchResponse = null;
		FileUtilities utils = new FileUtilities();
		ArrayList<File> filesToAdd = new ArrayList<>();
		log.info("Getting notification details...");
		String notificationDetails = notificationApiHandler.getNotificationDetails(notificationURL, iun);
		log.info("Notification details retrieved in {} ms, getting legal facts and notification documents metadata...", System.currentTimeMillis() - serviceStartTime);
		long performanceMillis = System.currentTimeMillis();
		Map<String, String> legalFactIds = notificationApiHandler.getLegalFactIdsAndTimestamp(notificationDetails);
        String dateIn3Months = OffsetDateTime.parse(legalFactIds.get("timestamp")).plusMonths(3).toString();
        ArrayList<String> docIdxs = notificationApiHandler.getDocumentIds(notificationDetails);
        ArrayList<PaymentDocumentData> paymentData = notificationApiHandler.getPaymentKeys(notificationDetails);
        LegalFactDownloadMetadataResponseDto legalFactDownload = notificationApiHandler.getLegalFactMetadata(legalFactDownloadMetadataURL, iun, legalFactIds.get("legalFactId"), legalFactIds.get("legalFactType"));
        if(legalFactDownload.getUrl() == null) {
        	log.info("No URL for {} file download provided, retrying in {} seconds", legalFactDownload.getFilename(), legalFactDownload.getRetryAfter());
	    	TimeUnit.SECONDS.sleep(legalFactDownload.getRetryAfter());
	    	log.info("Retrying to get download URL");
	    	legalFactDownload = notificationApiHandler.getLegalFactMetadata(legalFactDownloadMetadataURL, iun, legalFactIds.get("legalFactId"), legalFactIds.get("legalFactType"));
        }
        log.info("Legal facts and notification documents metadata retrieved in {} ms, getting physical files...", System.currentTimeMillis() - performanceMillis);
    	byte[] legalFactByteArr = notificationApiHandler.getFile(legalFactDownload.getUrl());
		File legalFactFile = utils.getFile(Constants.LEGAL_FACT_FILE_NAME, Constants.PDF_EXTENSION);
		FileUtils.writeByteArrayToFile(legalFactFile, legalFactByteArr);
		filesToAdd.add(legalFactFile);
		performanceMillis = System.currentTimeMillis();
		for(String currentDocId : docIdxs) {
			NotificationAttachmentDownloadMetadataResponseDto notificationDocumentMetadata = notificationApiHandler.getNotificationDocumentsMetadata(notificationAttachmentDownloadMetadataURL, iun, currentDocId);
			if(notificationDocumentMetadata.getUrl() == null) {
				log.info("No URL for {} file download provided, retrying in {} seconds", notificationDocumentMetadata.getFilename(), notificationDocumentMetadata.getRetryAfter());
				TimeUnit.SECONDS.sleep(notificationDocumentMetadata.getRetryAfter());
				log.info("Retrying to get download URL");
				notificationDocumentMetadata = notificationApiHandler.getNotificationDocumentsMetadata(notificationAttachmentDownloadMetadataURL, iun, currentDocId);
			}
			byte[] notificationDocumentByteArr = notificationApiHandler.getFile(notificationDocumentMetadata.getUrl());
			File notificationDocumentFile = utils.getFile(Constants.NOTIFICAION_DOCUMENT_FILE_NAME, Constants.PDF_EXTENSION);
			FileUtils.writeByteArrayToFile(notificationDocumentFile, notificationDocumentByteArr);
			filesToAdd.add(notificationDocumentFile);
		}
		for (int recipients = 0; recipients < paymentData.size(); recipients++) {
			Map<String, String> paymentKeys = paymentData.get(recipients).getPaymentKeys();
			for (String key : paymentKeys.keySet()) {
				if (paymentKeys.get(key) != null) {
					NotificationAttachmentDownloadMetadataResponseDto paymentDocumentMetadata = notificationApiHandler.getPaymentDocumentsMetadata(paymentAttachmentDownloadMetadataURL, iun, recipients, paymentKeys.get(key));
					if(paymentDocumentMetadata.getUrl() == null) {
						log.info("No URL for {} file download provided, retrying in {} seconds", paymentDocumentMetadata.getFilename(), paymentDocumentMetadata.getRetryAfter());
						TimeUnit.SECONDS.sleep(paymentDocumentMetadata.getRetryAfter());
						log.info("Retrying to get download URL");
						paymentDocumentMetadata = notificationApiHandler.getPaymentDocumentsMetadata(paymentAttachmentDownloadMetadataURL, iun, recipients, paymentKeys.get(key));
					}
					byte[] paymentDocumentByteArr = notificationApiHandler.getFile(paymentDocumentMetadata.getUrl());
					File paymentDocumentFile = utils.getFile(Constants.PAYMENT_DOCUMENT_FILE_NAME, Constants.PDF_EXTENSION);
					FileUtils.writeByteArrayToFile(paymentDocumentFile, paymentDocumentByteArr);
					filesToAdd.add(paymentDocumentFile);
				}
			}
		}
		log.info("Files retrieved in {} ms, constructing Opensearch query...", System.currentTimeMillis() - performanceMillis);
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("iun.keyword", iun);
		OpenSearchQuerydata queryData = queryConstructor.prepareQueryData("pn-logs", queryParams,
				new OpenSearchRangeQueryData("@timestamp", legalFactIds.get("timestamp"), dateIn3Months),
				new OpenSearchSortFilter("@timestamp", SortOrders.ASC));
        ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
        listOfQueryData.add(queryData);
        String query = queryConstructor.createBooleanMultiSearchQuery(listOfQueryData);
        log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
        performanceMillis = System.currentTimeMillis();
		openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		log.info("Query execution completed in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
		DownloadArchiveResponseDto response = ResponseConstructor.createNotificationLogResponse(openSearchResponse, filesToAdd, Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Notification data retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
		return response;
	}
		
	public DownloadArchiveResponseDto getDeanonymizedPersonLogs(RecipientTypes recipientType, String dateFrom, String dateTo, String ticketNumber, String taxid, String iun) throws IOException {
		log.info("Deanonymized logs retrieve process - START - user={}, ticket number={}", MDC.get("user_identifier"), ticketNumber);
		long serviceStartTime = System.currentTimeMillis();
		ArrayList<String> openSearchResponse = null;
		long performanceMillis = 0;
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<OpenSearchQuerydata>();
		HashMap<String, Object> queryParams = new HashMap<String, Object>();
		String query = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		ArrayList<String> deanonymizedOpenSearchResponse = new ArrayList<String>();
		String notificationDetails = notificationApiHandler.getNotificationDetails(notificationURL, iun);	
		String legalStartDate = notificationApiHandler.getLegalStartDate(notificationDetails);
		String dateIn3Months = OffsetDateTime.parse(legalStartDate).plusMonths(3).toString();
		//use case 3
		if (dateFrom != null && dateTo != null && taxid != null && recipientType!=null && ticketNumber!=null && iun==null) {
			log.info("Getting activities' deanonymized history, user={}, startDate={}, endDate={}, calling deanonimization service...", taxid, dateFrom, dateTo);
			GetBasicDataResponseDto internalidDto = deanonimizationApiHandler.getUniqueIdentifierForPerson(recipientType, taxid, getUniqueIdURL);
			log.info("Returned deanonimized data: " + internalidDto.toString());
			queryParams.put("uid", internalidDto.getData());
			queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
			log.info("Constructing Opensearch query...");
			performanceMillis = System.currentTimeMillis();
			query = queryConstructor.createBooleanMultiSearchQuery(queryData);
			log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			log.info("Query execution completed in {} ms, Deanonymizing results...", System.currentTimeMillis() - performanceMillis);
			performanceMillis = System.currentTimeMillis();
			deanonymizedOpenSearchResponse = OpenSearchUtil.toDeanonymizedDocuments(openSearchResponse, getTaxCodeURL, deanonimizationApiHandler);	
		} else{
			//use case 4
			if (iun!=null && ticketNumber!=null) {
				log.info("Getting deanonymized path, notification={}", iun);
				queryParams.put("iun.keyword", iun);
				queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
						new OpenSearchRangeQueryData("@timestamp", legalStartDate, dateIn3Months), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
				log.info("Constructing Opensearch query...");
				query = queryConstructor.createBooleanMultiSearchQuery(queryData);
				log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
				performanceMillis = System.currentTimeMillis();
				openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
				log.info("Query execution completed in {} ms, Deanonymizing results...", System.currentTimeMillis() - performanceMillis);
				performanceMillis = System.currentTimeMillis();
				deanonymizedOpenSearchResponse = OpenSearchUtil.toDeanonymizedDocuments(openSearchResponse, getTaxCodeURL, deanonimizationApiHandler);
			}
		}
		log.info("Deanonymization completed in {} ms, Constructing service response...", System.currentTimeMillis() - performanceMillis);
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(deanonymizedOpenSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Deanonymized logs retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
		return response;
	}
}
