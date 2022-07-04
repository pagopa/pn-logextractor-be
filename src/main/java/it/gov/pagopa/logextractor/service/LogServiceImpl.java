package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.dto.LegalFactBasicData;
import it.gov.pagopa.logextractor.dto.LegalFactData;
import it.gov.pagopa.logextractor.dto.NotificationCsvBean;
import it.gov.pagopa.logextractor.dto.NotificationGeneralData;
import it.gov.pagopa.logextractor.dto.PaymentDocumentData;
import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.dto.response.LegalFactDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationAttachmentDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.util.CommonUtilities;
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
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;
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
	
	@Value("${external.selfcare.getEncodedIpaCode.url}")
	String selfCareEncodedIpaCodeURL;
	
	@Value("${external.notification.getLegalFactDownloadMetadata.url}")
	String legalFactDownloadMetadataURL;
	
	@Value("${external.notification.getNotificationAttachmentDownloadMetadata.url}")
	String notificationAttachmentDownloadMetadataURL;
	
	@Value("${external.notification.getPaymentAttachmentDownloadMetadata.url}")
	String paymentAttachmentDownloadMetadataURL;
	
	@Value("${external.selfcare.getPublicAuthorityName.url}")
	String getPublicAuthorityNameUrl;
	
	@Autowired
	NotificationApiHandler notificationApiHandler;
	
	@Autowired
	OpenSearchApiHandler openSearchApiHandler;
	
	@Autowired
	DeanonimizationApiHandler deanonimizationApiHandler;

	@Override
	public DownloadArchiveResponseDto getAnonymizedPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun, String personId) throws IOException {
		log.info("Anonymized logs retrieve process - START - user={}, ticket number={}, internalId={}, startDate={}, endDate={}, iun={}", MDC.get("user_identifier"), ticketNumber, personId, dateFrom, dateTo, iun);
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
		ArrayList<String> openSearchResponse = null;
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<OpenSearchQuerydata>();
		HashMap<String, Object> queryParams = new HashMap<String, Object>();
		String query = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && iun == null) {
			log.info("Getting activities' anonymized history - START - constructing Opensearch query...");
			queryParams.put("uid.keyword", personId);
			queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
			query = queryConstructor.createBooleanMultiSearchQuery(queryData);
			log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
			performanceMillis = System.currentTimeMillis();
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		} else {
			// use case 8
			if (iun != null && ticketNumber!=null) {
				log.info("Notification's anonymized path recovery - START - getting notification details...");
				NotificationDetailsResponseDto notificationDetails = notificationApiHandler.getNotificationDetails(notificationURL, iun);
				log.info("Service response: notificationDetails={}", new ObjectMapper().writer().writeValueAsString(notificationDetails));
				log.info("Notification details retrieved in {} ms, constructing Opensearch query...", System.currentTimeMillis() - serviceStartTime);
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
		log.info("Monthly notifications retrieve process - START - user={}, ticket number={}, reference month={}, IPA code={}", MDC.get("user_identifier"), ticketNumber, referenceMonth, ipaCode);
		long serviceStartTime = System.currentTimeMillis();
		CommonUtilities commonUtils = new CommonUtilities();
		LocalDate startDate = LocalDate.parse(StringUtils.removeIgnoreCase(referenceMonth, "-")+"01", DateTimeFormatter.BASIC_ISO_DATE);
		LocalDate endDate = startDate.plusMonths(1);
		String finaldatePart = "T00:00:00.000Z";
		log.info("Getting public authority id...");
		long performanceMillis = System.currentTimeMillis();
		String encodedIpaCode = deanonimizationApiHandler.getEncodedIpaCode(selfCareEncodedIpaCodeURL, ipaCode);
		log.info("Service response: publicAuthorityId={}", encodedIpaCode);
		ArrayList<NotificationCsvBean> notifications = new ArrayList<NotificationCsvBean>();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("startDate", startDate.toString()+finaldatePart);
        parameters.put("endDate", endDate.toString()+finaldatePart);
        parameters.put("size", 100);
        log.info("Public authority id retrieved in {} ms, getting notifications general data, publicAuthority={}, startDate={}, endDate={}", System.currentTimeMillis() - performanceMillis, encodedIpaCode, startDate, endDate);
        performanceMillis = System.currentTimeMillis();
		ArrayList<NotificationGeneralData> notificationsGeneralData = notificationApiHandler.getNotificationsByPeriod(notificationURL,
																		parameters, encodedIpaCode, new ArrayList<NotificationGeneralData>(), 
																		"", new ArrayList<String>(), MDC.get("user_identifier"));
		if(notificationsGeneralData != null) {
			log.info("Notifications general data retrieved in {} ms, getting notifications' details", System.currentTimeMillis() - performanceMillis);
			performanceMillis = System.currentTimeMillis();
			for(NotificationGeneralData nTemp : notificationsGeneralData) {
				NotificationDetailsResponseDto notificationDetails = notificationApiHandler.getNotificationDetails(notificationURL, nTemp.getIun());
				NotificationCsvBean notification = new NotificationCsvBean();
				
				if(null != nTemp.getRecipients() && nTemp.getRecipients().size() > 0) {
					StringBuilder recipientsBuilder = new StringBuilder();
					for(String tempRecipient : nTemp.getRecipients()) {
						recipientsBuilder.append(tempRecipient + "-");
					}
					recipientsBuilder.deleteCharAt(recipientsBuilder.length()-1);
					notification.setCodici_fiscali(commonUtils.escapeForCsv(recipientsBuilder.toString()));
					recipientsBuilder.setLength(0);
				}
				notification.setIUN(commonUtils.escapeForCsv(nTemp.getIun()));
				notification.setData_invio(commonUtils.escapeForCsv(nTemp.getSentAt()));
				notification.setData_generazione_attestazione_opponibile_a_terzi(commonUtils.escapeForCsv(notificationApiHandler.getLegalStartDate(notificationDetails)));
				notification.setOggetto(commonUtils.escapeForCsv(nTemp.getSubject()));
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
		log.info("Anonymized logs retrieve process - START - user={}, traceId={} startDate={}, endDate={}", MDC.get("user_identifier"), traceId, dateFrom, dateTo);
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
		ArrayList<String> openSearchResponse = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		//use case 10
		if (dateFrom != null && dateTo != null && traceId != null) {
			log.info("Getting anonymized logs, constructing Opensearch query...");
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
		log.info("Notification data retrieve process - START - user={}, ticket number={}, iun={}", MDC.get("user_identifier"), ticketNumber, iun);
		long serviceStartTime = System.currentTimeMillis();
        OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
        ArrayList<String> openSearchResponse = null;
		FileUtilities utils = new FileUtilities();
		ArrayList<File> filesToAdd = new ArrayList<>();
		log.info("Getting notification details...");
		NotificationDetailsResponseDto notificationDetails = notificationApiHandler.getNotificationDetails(notificationURL, iun);
		String legalStartDate = notificationApiHandler.getLegalStartDate(notificationDetails);
		String dateIn3Months = OffsetDateTime.parse(legalStartDate).plusMonths(3).toString();
		log.info("Service response: notificationDetails={}", new ObjectMapper().writer().writeValueAsString(notificationDetails));
		log.info("Notification details retrieved in {} ms, getting legal facts metadata...", System.currentTimeMillis() - serviceStartTime);
		long performanceMillis = System.currentTimeMillis();
		ArrayList<LegalFactData> legalFactIds = notificationApiHandler.getLegalFactIdsAndTimestamp(notificationDetails);
		ArrayList<LegalFactDownloadMetadataResponseDto> legalFactToDownloadList = new ArrayList<>();
		for(LegalFactData legalFact : legalFactIds) {
			for(LegalFactBasicData legalFactBasicData : legalFact.getBasicData()) {
				LegalFactDownloadMetadataResponseDto legalFactToDownload = notificationApiHandler.getLegalFactMetadata(legalFactDownloadMetadataURL, iun, legalFactBasicData.getKey(), legalFactBasicData.getCategory());
		        if(legalFactToDownload.getUrl() == null) {
		        	log.info("No URL for {} file download provided, retrying in {} seconds", legalFactToDownload.getFilename(), legalFactToDownload.getRetryAfter());
			    	TimeUnit.SECONDS.sleep(legalFactToDownload.getRetryAfter());
			    	log.info("Retrying to get download URL");
			    	legalFactToDownload = notificationApiHandler.getLegalFactMetadata(legalFactDownloadMetadataURL, iun, legalFactBasicData.getKey(), legalFactBasicData.getCategory());
		        }
		        legalFactToDownloadList.add(legalFactToDownload);
			}
		}
		log.info("Service response: legalFact list={}", legalFactToDownloadList.toString());
		log.info("Legal facts metadata retrieved in {} ms, getting notification documents metadata...", System.currentTimeMillis() - performanceMillis);
		performanceMillis = System.currentTimeMillis();
        ArrayList<String> docIdxs = notificationApiHandler.getDocumentIds(notificationDetails);
        log.info("Service response: document idx list={}", docIdxs.toString());
        log.info("Notification documents metadata retrieved in {} ms, getting payment documents metadata...", System.currentTimeMillis() - performanceMillis);
        performanceMillis = System.currentTimeMillis();
        ArrayList<PaymentDocumentData> paymentData = notificationApiHandler.getPaymentKeys(notificationDetails);
        log.info("Service response: paymentDocument list={}", paymentData.toString());
        log.info("Payment documents metadata retrieved in {} ms, getting physical files...", System.currentTimeMillis() - performanceMillis);
        performanceMillis = System.currentTimeMillis();
        for(LegalFactDownloadMetadataResponseDto legalFactMetadata : legalFactToDownloadList) {
    		byte[] legalFactByteArr = notificationApiHandler.getFile(legalFactMetadata.getUrl());
    		File legalFactFile = utils.getFile(legalFactMetadata.getFilename(), Constants.PDF_EXTENSION);
    		FileUtils.writeByteArrayToFile(legalFactFile, legalFactByteArr);
    		filesToAdd.add(legalFactFile);
    	}
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
			File notificationDocumentFile = utils.getFile(notificationDocumentMetadata.getFilename(), Constants.PDF_EXTENSION);
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
					File paymentDocumentFile = utils.getFile(paymentDocumentMetadata.getFilename(), Constants.PDF_EXTENSION);
					FileUtils.writeByteArrayToFile(paymentDocumentFile, paymentDocumentByteArr);
					filesToAdd.add(paymentDocumentFile);
				}
			}
		}
		log.info("Physical files retrieved in {} ms, constructing Opensearch query...", System.currentTimeMillis() - performanceMillis);
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("iun.keyword", iun);
		OpenSearchQuerydata queryData = queryConstructor.prepareQueryData("pn-logs", queryParams,
				new OpenSearchRangeQueryData("@timestamp", legalStartDate, dateIn3Months),
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
		log.info("Deanonymized logs retrieve process - START - user={}, ticket number={}, taxId={}, startDate={}, endDate={}, iun={}", MDC.get("user_identifier"), ticketNumber, taxid, dateFrom, dateTo, iun);
		long serviceStartTime = System.currentTimeMillis();
		ArrayList<String> openSearchResponse = null;
		long performanceMillis = 0;
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<OpenSearchQuerydata>();
		HashMap<String, Object> queryParams = new HashMap<String, Object>();
		String query = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		ArrayList<String> deanonymizedOpenSearchResponse = new ArrayList<String>();	
		//use case 3
		if (dateFrom != null && dateTo != null && taxid != null && recipientType!=null && ticketNumber!=null && iun==null) {
			log.info("Getting activities' de-anonymized history - START - getting internal id...");
			GetBasicDataResponseDto internalIdDto = deanonimizationApiHandler.getUniqueIdentifierForPerson(recipientType, taxid, getUniqueIdURL);
			log.info("Service response: internalId={} " + internalIdDto.getData());
			log.info("Internal id retrieved in {} ms, constructing Opensearch query...", System.currentTimeMillis() - serviceStartTime);
			queryParams.put("uid.keyword", internalIdDto.getData());
			queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
			query = queryConstructor.createBooleanMultiSearchQuery(queryData);
			log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
			performanceMillis = System.currentTimeMillis();
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			log.info("Query execution completed in {} ms, de-anonymizing results...", System.currentTimeMillis() - performanceMillis);
			performanceMillis = System.currentTimeMillis();
			deanonymizedOpenSearchResponse = deanonimizationApiHandler.toDeanonymizedDocuments(openSearchResponse, getTaxCodeURL, getPublicAuthorityNameUrl);	
		} else{
			//use case 4
			if (iun!=null && ticketNumber!=null) {
				log.info("Getting de-anonymized path - START - getting notification details...");
				NotificationDetailsResponseDto notificationDetails = notificationApiHandler.getNotificationDetails(notificationURL, iun);
				String legalStartDate = notificationApiHandler.getLegalStartDate(notificationDetails);
				log.info("Service response: notificationDetails={}", new ObjectMapper().writer().writeValueAsString(notificationDetails));
				log.info("Notification details retrieved in {} ms, constructing Opensearch query...", System.currentTimeMillis() - serviceStartTime);
				String dateIn3Months = OffsetDateTime.parse(legalStartDate).plusMonths(3).toString();
				queryParams.put("iun.keyword", iun);
				queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
						new OpenSearchRangeQueryData("@timestamp", legalStartDate, dateIn3Months), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
				query = queryConstructor.createBooleanMultiSearchQuery(queryData);
				log.info("Executing query:"+ RegExUtils.removeAll(query, "\n"));
				performanceMillis = System.currentTimeMillis();
				openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
				log.info("Query execution completed in {} ms, de-anonymizing results...", System.currentTimeMillis() - performanceMillis);
				performanceMillis = System.currentTimeMillis();
				deanonymizedOpenSearchResponse = deanonimizationApiHandler.toDeanonymizedDocuments(openSearchResponse, getTaxCodeURL, getPublicAuthorityNameUrl);
			}
		}
		log.info("Deanonymization completed in {} ms, Constructing service response...", System.currentTimeMillis() - performanceMillis);
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(deanonymizedOpenSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Deanonymized logs retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
		return response;
	}
}
