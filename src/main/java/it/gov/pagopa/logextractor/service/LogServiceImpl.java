package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.dto.response.DownloadNotPossibleYetResponseDTO;
import it.gov.pagopa.logextractor.dto.response.FileDownloadMetadataResponseDTO;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationHistoryResponseDTO;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.ResponseConstructor;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonymizationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation class of {@link LogService}
 */
@Service
@Slf4j
public class LogServiceImpl implements LogService {
	
	@Autowired
	NotificationApiHandler notificationApiHandler;
	
	@Autowired
	OpenSearchApiHandler openSearchApiHandler;
	
	@Autowired
	DeanonymizationApiHandler deanonimizationApiHandler;

	@Override
	public DownloadArchiveResponseDto getAnonymizedPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun, String personId) throws IOException {
		log.info("Anonymized logs retrieve process - START - user={}, ticket number={}, internalId={}, startDate={}, endDate={}, iun={}", MDC.get("user_identifier"), ticketNumber, personId, dateFrom, dateTo, iun);
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
		List<String> openSearchResponse = null;
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && iun == null) {
			log.info("Getting activities' anonymized history... ");
			performanceMillis = System.currentTimeMillis();
			openSearchResponse = openSearchApiHandler.getAnonymizedLogsByUid(personId, dateFrom, dateTo);
		} else {
			// use case 8
			if (iun != null && ticketNumber!=null) {
				log.info("Getting notification details...");
				NotificationDetailsResponseDto notificationDetails = notificationApiHandler.getNotificationDetails(iun);
				log.info("Service response: notificationDetails={} retrieved in {} ms", new ObjectMapper().writer().writeValueAsString(notificationDetails), System.currentTimeMillis() - serviceStartTime);
				OffsetDateTime notificationStartDate = OffsetDateTime.parse(notificationDetails.getSentAt());
				String notificationEndDate = notificationStartDate.plusMonths(3).toString();
				performanceMillis = System.currentTimeMillis();
				openSearchResponse = openSearchApiHandler.getAnonymizedLogsByIun(iun, notificationStartDate.toString(), notificationEndDate);
			}
		}
		log.info("Query execution completed in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(openSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Anonymized logs retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
		return response;
	}

	@Override
	public DownloadArchiveResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String endMonth, String publicAuthorityName) throws IOException, ParseException,CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, LogExtractorException {
		log.info("Monthly notifications retrieve process - START - user={}, ticket number={}, reference month={}, end month={}, public authority name={}", MDC.get("user_identifier"), ticketNumber, referenceMonth, endMonth, publicAuthorityName);
		long serviceStartTime = System.currentTimeMillis();
		FileUtilities utils = new FileUtilities();
		List<File> csvFiles = new ArrayList<>();
		log.info("Getting public authority id...");
		long performanceMillis = System.currentTimeMillis();
		String encodedPublicAuthorityName = deanonimizationApiHandler.getPublicAuthorityId(publicAuthorityName);
        log.info("Public authority id retrieved in {} ms, getting notifications, publicAuthority={}, startDate={}, endDate={}", System.currentTimeMillis() - performanceMillis, encodedPublicAuthorityName, referenceMonth, endMonth);
        performanceMillis = System.currentTimeMillis();
		List<NotificationData> notifications = notificationApiHandler.getNotificationsByMonthsPeriod(referenceMonth, endMonth, 
				encodedPublicAuthorityName,  MDC.get("user_identifier"));
		log.info("{} notifications retrieved in {} ms, constructing service response...", notifications.size(), System.currentTimeMillis() - performanceMillis);
		if(null != notifications && !notifications.isEmpty()) {
			int numberOfFiles = (int)Math.ceil(((double)notifications.size())/Constants.CSV_FILE_MAX_ROWS);
			int notificationPlaceholder = 0;
			while(numberOfFiles > 0) {
				if(numberOfFiles == 1) {
					List<NotificationData> notificationsPartition = notifications.subList(notificationPlaceholder, notifications.size());
					File file = utils.getFile(Constants.NOTIFICATION_CSV_FILE_NAME,Constants.CSV_EXTENSION);
					utils.writeCsv(file, utils.toCsv(notificationsPartition));
					csvFiles.add(file);
					numberOfFiles--;
				}
				else {
					List<NotificationData> notificationsPartition = notifications.subList(notificationPlaceholder,
							notificationPlaceholder+Constants.CSV_FILE_MAX_ROWS);
					File file = utils.getFile(Constants.NOTIFICATION_CSV_FILE_NAME,Constants.CSV_EXTENSION);
					utils.writeCsv(file, utils.toCsv(notificationsPartition));
					csvFiles.add(file);
					notificationPlaceholder += Constants.CSV_FILE_MAX_ROWS;
					numberOfFiles--;
				}
			}
		}
		DownloadArchiveResponseDto response = ResponseConstructor.createCsvLogResponse(csvFiles, Constants.ZIP_ARCHIVE_NAME);
		log.info("Monthly notifications retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
		return response;
	}
	
	@Override
	public DownloadArchiveResponseDto getTraceIdLogs(String dateFrom, String dateTo, String traceId) throws IOException {
		log.info("Anonymized logs retrieve process - START - user={}, traceId={}, startDate={}, endDate={}", MDC.get("user_identifier"), traceId, dateFrom, dateTo);
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
		List<String> openSearchResponse = null;
		//use case 10
		if (dateFrom != null && dateTo != null && traceId != null) {
			log.info("Getting anonymized logs");
			performanceMillis = System.currentTimeMillis();
			openSearchResponse = openSearchApiHandler.getAnonymizedLogsByTraceId(traceId, dateFrom, dateTo);
		}
		log.info("Query execution completed in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(openSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Anonymized logs retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
		return response;
	}
	
	@Override
	public Object getNotificationInfoLogs(String ticketNumber, String iun) throws IOException, InterruptedException {
		log.info("Notification data retrieve process - START - user={}, ticket number={}, iun={}", MDC.get("user_identifier"), ticketNumber, iun);
		ArrayList<String> downloadKeys = new ArrayList<>();
		ArrayList<String> downloadUrls = new ArrayList<>();
		long serviceStartTime = System.currentTimeMillis();
		double secondsToWait = 0;
		ObjectMapper mapper = new ObjectMapper();
		FileUtilities utils = new FileUtilities();
		ArrayList<File> filesToAdd = new ArrayList<>();
		log.info("Getting notification details...");
		NotificationDetailsResponseDto notificationDetails = notificationApiHandler.getNotificationDetails(iun);
		OffsetDateTime notificationStartDate = OffsetDateTime.parse(notificationDetails.getSentAt());
		String notificationEndDate = notificationStartDate.plusMonths(3).toString();
		log.info("Service response: notificationDetails={} retrieved in {} ms, getting history data...", mapper.writer().writeValueAsString(notificationDetails), System.currentTimeMillis() - serviceStartTime);
		NotificationHistoryResponseDTO notificationHistory = notificationApiHandler.getNotificationHistory(iun, notificationDetails.getRecipients().size(), notificationStartDate.toString());
		log.info("Service response: notificationHistory={} retrieved in {} ms, getting legal facts' keys...", mapper.writer().writeValueAsString(notificationHistory), System.currentTimeMillis() - serviceStartTime);
		long performanceMillis = System.currentTimeMillis();
		downloadKeys.addAll(notificationApiHandler.getLegalFactKeys(notificationHistory));
		log.info("Legal facts' keys retrieved in {} ms, getting notification documents' keys...", System.currentTimeMillis() - performanceMillis);
		performanceMillis = System.currentTimeMillis();
		downloadKeys.addAll(notificationApiHandler.getDocumentKeys(notificationDetails));
        log.info("Notification documents' keys retrieved in {} ms, getting payment documents' keys...", System.currentTimeMillis() - performanceMillis);
        performanceMillis = System.currentTimeMillis();
        downloadKeys.addAll(notificationApiHandler.getPaymentKeys(notificationDetails));
        log.info("Notification payment' keys retrieved in {} ms, getting downloads' metadata...", System.currentTimeMillis() - performanceMillis);
        performanceMillis = System.currentTimeMillis();
        for(String key : downloadKeys) {
        	FileDownloadMetadataResponseDTO downloadData = notificationApiHandler.getDownloadMetadata(key);
        	downloadUrls.add(downloadData.getDownload().getUrl());
        	if(null != downloadData && null != downloadData.getDownload() 
        			&& null == downloadData.getDownload().getUrl() && null != downloadData.getDownload().getRetryAfter()
        			&& secondsToWait < downloadData.getDownload().getRetryAfter()) {
        		secondsToWait = downloadData.getDownload().getRetryAfter();
        	}
        }
        if(secondsToWait > 0) {
        	log.info("Notification downloads' metadata retrieved in {} ms, physical files aren't ready yet. Constructing service response...", System.currentTimeMillis() - performanceMillis);
        	int timeToWaitInMinutes = (int)Math.ceil(secondsToWait/60);
        	DownloadNotPossibleYetResponseDTO response = DownloadNotPossibleYetResponseDTO
        			.builder()
        			.message(Constants.OPERATION_CANNOT_BE_COMPLETED_MESSAGE + timeToWaitInMinutes + (timeToWaitInMinutes > 1 ? Constants.MINUTES_LABEL : Constants.MINUTE_LABEL))
        			.build();
        	log.info("Notification data retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
        	return response;
        }
        else {
        	log.info("Notification downloads' metadata retrieved in {} ms, getting physical files... ", System.currentTimeMillis() - performanceMillis);
        	performanceMillis = System.currentTimeMillis();
        	for(String url : downloadUrls) {
        		byte[] externalFile = notificationApiHandler.getFile(url);
        		File downloadedFile = utils.getFile(Constants.DOCUMENT_LABEL, Constants.PDF_EXTENSION);
        		FileUtils.writeByteArrayToFile(downloadedFile, externalFile);
        		filesToAdd.add(downloadedFile);
        	}
        	log.info("Physical files retrieved in {} ms", System.currentTimeMillis() - performanceMillis);
        	List<String> openSearchResponse = openSearchApiHandler.getAnonymizedLogsByIun(iun, notificationStartDate.toString(), notificationEndDate);
    		log.info("Query execution completed in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
    		DownloadArchiveResponseDto response = ResponseConstructor.createNotificationLogResponse(openSearchResponse, filesToAdd, Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
    		log.info("Notification data retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
    		return response;
        }
	}
		
	public DownloadArchiveResponseDto getDeanonymizedPersonLogs(RecipientTypes recipientType, String dateFrom, String dateTo, String ticketNumber, String taxid, String iun) throws IOException, LogExtractorException {
		log.info("Deanonymized logs retrieve process - START - user={}, ticket number={}, taxId={}, startDate={}, endDate={}, iun={}", MDC.get("user_identifier"), ticketNumber, taxid, dateFrom, dateTo, iun);
		long serviceStartTime = System.currentTimeMillis();
		List<String> openSearchResponse = null;
		long performanceMillis = 0;
		List<String> deanonymizedOpenSearchResponse = new ArrayList<>();	
		//use case 3
		if (dateFrom != null && dateTo != null && taxid != null && recipientType!=null && ticketNumber!=null && iun==null) {
			log.info("Getting internal id...");
			String internalId = deanonimizationApiHandler.getUniqueIdentifierForPerson(recipientType, taxid);
			log.info("Service response: internalId={} retrieved in {} ms", internalId, System.currentTimeMillis() - serviceStartTime);
			performanceMillis = System.currentTimeMillis();
			openSearchResponse = openSearchApiHandler.getAnonymizedLogsByUid(internalId, dateFrom, dateTo);
			log.info("Query execution completed in {} ms, de-anonymizing results...", System.currentTimeMillis() - performanceMillis);
			performanceMillis = System.currentTimeMillis();
			deanonymizedOpenSearchResponse = deanonimizationApiHandler.deanonymizeDocuments(openSearchResponse, recipientType);	
		} else{
			//use case 4
			if (iun!=null && ticketNumber!=null) {
				log.info("Getting notification details...");
				NotificationDetailsResponseDto notificationDetails = notificationApiHandler.getNotificationDetails(iun);
				log.info("Service response: notificationDetails={} retrieved in {} ms", new ObjectMapper().writer().writeValueAsString(notificationDetails), System.currentTimeMillis() - serviceStartTime);
				OffsetDateTime notificationStartDate = OffsetDateTime.parse(notificationDetails.getSentAt());
				String notificationEndDate = notificationStartDate.plusMonths(3).toString();
				performanceMillis = System.currentTimeMillis();
				openSearchResponse = openSearchApiHandler.getAnonymizedLogsByIun(iun, notificationStartDate.toString(), notificationEndDate);
				log.info("Query execution completed in {} ms, de-anonymizing results...", System.currentTimeMillis() - performanceMillis);
				performanceMillis = System.currentTimeMillis();
				deanonymizedOpenSearchResponse = deanonimizationApiHandler.deanonymizeDocuments(openSearchResponse, RecipientTypes.PF);
			}
		}
		log.info("Deanonymization completed in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(deanonymizedOpenSearchResponse,Constants.LOG_FILE_NAME, Constants.ZIP_ARCHIVE_NAME);
		log.info("Deanonymized logs retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
		return response;
	}
}
