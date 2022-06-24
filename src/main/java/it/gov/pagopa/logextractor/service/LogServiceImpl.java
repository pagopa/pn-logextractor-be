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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.NotificationCsvBean;
import it.gov.pagopa.logextractor.dto.NotificationGeneralData;
import it.gov.pagopa.logextractor.dto.PaymentDocumentData;
import it.gov.pagopa.logextractor.dto.request.DownloadArchiveResponseDto;
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

@Service
public class LogServiceImpl implements LogService{
	//
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

		ArrayList<String> openSearchResponse = null;
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<OpenSearchQuerydata>();
		HashMap<String, Object> queryParams = new HashMap<String, Object>();
		String query = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && iun == null) {
			queryParams.put("uid", personId);
			queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
			query = queryConstructor.createBooleanMultiSearchQuery(queryData);
			System.out.println("Query:\n" + query);
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		} else {
			// use case 8
			if (iun != null) {
				String legalStartDate = notificationApiHandler.getNotificationLegalStartDate(notificationURL, iun);	
				String dateIn3Months = OffsetDateTime.parse(legalStartDate).plusMonths(3).toString();
				queryParams.put("iun", iun);
				queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
						new OpenSearchRangeQueryData("@timestamp", legalStartDate, dateIn3Months), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
				query = queryConstructor.createBooleanMultiSearchQuery(queryData);
				System.out.println("Query:\n" + query);
				openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			}
		}
		
		return ResponseConstructor.createSimpleLogResponse(openSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
	}


	@Override
	public DownloadArchiveResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode) throws IOException, ParseException,CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		LocalDate startDate = LocalDate.parse(StringUtils.removeIgnoreCase(referenceMonth, "-")+"01", DateTimeFormatter.BASIC_ISO_DATE);
		LocalDate endDate = startDate.plusMonths(1);
		String finaldatePart = "T00:00:00.000Z";
		String encodedIpaCode = selfCareApiHandler.getEncodedIpaCode(finaldatePart, ipaCode);
		ArrayList<NotificationCsvBean> notifications = new ArrayList<NotificationCsvBean>();
		ArrayList<NotificationGeneralData> notificationsGeneralData = notificationApiHandler.getNotificationsByPeriod(notificationURL, 
																		encodedIpaCode,
																		startDate.toString()+finaldatePart, 
																		endDate.toString()+finaldatePart, 
																		100);
		if(notificationsGeneralData != null) {
			for(NotificationGeneralData nTemp : notificationsGeneralData) {
				String legalStartDate = notificationApiHandler.getNotificationLegalStartDate(notificationURL, nTemp.getIun());
				ArrayList<String> taxIds = new ArrayList<String>();
				taxIds.addAll(nTemp.getRecipients());
				NotificationCsvBean notification = NotificationCsvBean.builder()
													.iun(nTemp.getIun())
													.sendDate(nTemp.getSentAt())
													.attestationGenerationDate(legalStartDate)
													.subject(nTemp.getSubject())
													.taxIds(taxIds)
													.build();
				notifications.add(notification);
			}
		}
		return ResponseConstructor.createCsvLogResponse(notifications, Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
	}
	
	@Override
	public DownloadArchiveResponseDto getTraceIdLogs(String dateFrom, String dateTo, String traceId) throws IOException {
		ArrayList<String> openSearchResponse = null;
		OpenSearchQueryConstructor queryConstructor = new OpenSearchQueryConstructor();
		//use case 10
		if (dateFrom != null && dateTo != null && traceId != null) {
			System.out.println("use case 10");
			HashMap<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("root_trace_id", traceId);
			OpenSearchQuerydata queryData = queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC));
			ArrayList<OpenSearchQuerydata> listOfQueryData = new ArrayList<>();
			listOfQueryData.add(queryData);
			String query = queryConstructor.createBooleanMultiSearchQuery(listOfQueryData);
			System.out.println("Query:\n" + query);
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
		}

		return ResponseConstructor.createSimpleLogResponse(openSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		
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
//		String urlForLegalFactDownload = notificationHandler.getLegalFactMetadata(legalFactDownloadMetadataURL, iun, legalFactIds.get("legalFactId"), legalFactIds.get("legalFactType"));
		
		
//		System.out.println(urlForLegalFactDownload);
		
//		byte[] legalFactByteArr = notificationHandler.getFile(urlForLegalFactDownload);
//		File legalFactFile = utils.getFile(Constants.LEGAL_FACT_FILE_NAME, Constants.TXT_EXTENSION);
//		FileUtils.writeByteArrayToFile(legalFactFile, legalFactByteArr);

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
			GetBasicDataResponseDto internalidDto = deanonimizationApiHandler.getUniqueIdentifierForPerson(recipientType, taxid, getUniqueIdURL);
			System.out.println("use case 3");
			queryParams.put("uid", internalidDto.getData());
			queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
					new OpenSearchRangeQueryData("@timestamp", dateFrom, dateTo), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
			query = queryConstructor.createBooleanMultiSearchQuery(queryData);
			System.out.println("Query:\n" + query);
			openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
			deanonymizedOpenSearchResponse = OpenSearchUtil.toDeanonymizedDocuments(openSearchResponse, getTaxCodeURL);	
		} else{
			//use case 4
			if (iun!=null) {
				System.out.println("use case 4");
				queryParams.put("iun", iun);
				queryData.add(queryConstructor.prepareQueryData("pn-logs", queryParams, 
						new OpenSearchRangeQueryData("@timestamp", legalStartDate, dateIn3Months), new OpenSearchSortFilter("@timestamp", SortOrders.ASC)));
				query = queryConstructor.createBooleanMultiSearchQuery(queryData);
				System.out.println("Query:\n" + query);
				openSearchResponse = openSearchApiHandler.getDocumentsByMultiSearchQuery(query, openSearchURL, openSearchUsername, openSearchPassword);
				deanonymizedOpenSearchResponse = OpenSearchUtil.toDeanonymizedDocuments(openSearchResponse, getTaxCodeURL);
			}
		}
		return ResponseConstructor.createSimpleLogResponse(deanonymizedOpenSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);

	}
}
