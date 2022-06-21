package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.dto.NotificationCsvBean;
import it.gov.pagopa.logextractor.dto.NotificationGeneralData;
import it.gov.pagopa.logextractor.dto.request.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.PasswordFactory;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.ResponseConstructor;
import it.gov.pagopa.logextractor.util.SortOrders;
import it.gov.pagopa.logextractor.util.ZipFactory;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchQueryConstructor;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchQuerydata;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchRangeQueryData;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchSortFilter;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchUtil;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;
import it.gov.pagopa.logextractor.util.external.selfcare.SelfCareApiHandler;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;

@Service
public class LogServiceImpl implements LogService{
	
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
		
		return ResponseConstructor.createSimpleLogResponse(openSearchResponse,Constants.FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
	}


	@Override
	public DownloadArchiveResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode) throws IOException, ParseException,CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		LocalDate startDate = LocalDate.parse(StringUtils.removeIgnoreCase(referenceMonth, "-")+"01", DateTimeFormatter.BASIC_ISO_DATE);
		LocalDate endDate = startDate.plusMonths(1);
		String finaldatePart = "T00:00:00.000Z";
		String encodedIpaCode = selfCareApiHandler.getEncodedIpaCode(selfCareEncodedIpaCodeURL, ipaCode);
		ArrayList<NotificationCsvBean> notifications = new ArrayList<NotificationCsvBean>();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("startDate", startDate.toString()+finaldatePart);
        parameters.put("endDate", endDate.toString()+finaldatePart);
        parameters.put("size", 100);
		ArrayList<NotificationGeneralData> notificationsGeneralData = notificationApiHandler.getNotificationsByPeriod(notificationURL,
																		parameters, encodedIpaCode, 0, new ArrayList<NotificationGeneralData>());
		if(notificationsGeneralData != null) {
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
		return ResponseConstructor.createCsvLogResponse(notifications, Constants.FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
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

		return ResponseConstructor.createSimpleLogResponse(openSearchResponse,Constants.FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		
	}
	
	@Override
	public DownloadArchiveResponseDto getNotificationInfoLogs(String iun) throws IOException {
		// TODO Auto-generated method stub
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
		return ResponseConstructor.createSimpleLogResponse(deanonymizedOpenSearchResponse,Constants.FILE_NAME, Constants.ZIP_ARCHIVE_NAME);

	}
}
