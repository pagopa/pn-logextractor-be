package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDTO;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;
import it.gov.pagopa.logextractor.util.constant.CognitoConstants;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;
import org.apache.commons.io.FileUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.dto.response.FileDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationHistoryResponseDto;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import it.gov.pagopa.logextractor.util.ResponseConstructor;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;
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
	DeanonimizationApiHandler deanonimizationApiHandler;

	@Override
	public BaseResponseDTO getAnonymizedPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun, String personId) throws IOException, LogExtractorException {
		log.info("Anonymized logs retrieve process - START - user={}, ticket number={}, internalId={}, startDate={}, endDate={}, iun={}", MDC.get(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER), ticketNumber, personId, dateFrom, dateTo, iun);
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
		List<String> openSearchResponse = new ArrayList<>();
		// use case 7
		if (dateFrom != null && dateTo != null && personId != null && iun == null) {
			log.info("Getting activities' anonymized history... ");
			performanceMillis = System.currentTimeMillis();
			openSearchResponse = openSearchApiHandler.getAnonymizedLogsByUid(personId, dateFrom, dateTo);
		} else {
			// use case 8
			if (iun != null && ticketNumber!=null) {
				log.info(LoggingConstants.GET_NOTIFICATION_DETAILS);
				NotificationDetailsResponseDto notificationDetails = notificationApiHandler.getNotificationDetails(iun);
				log.info("Service response: notificationDetails={} retrieved in {} ms", new ObjectMapper().writer().writeValueAsString(notificationDetails), System.currentTimeMillis() - serviceStartTime);
				OffsetDateTime notificationStartDate = OffsetDateTime.parse(notificationDetails.getSentAt());
				String notificationEndDate = notificationStartDate.plusMonths(3).toString();
				performanceMillis = System.currentTimeMillis();
				openSearchResponse = openSearchApiHandler.getAnonymizedLogsByIun(iun, notificationStartDate.toString(), notificationEndDate);
			}
		}
		log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, System.currentTimeMillis() - performanceMillis, openSearchResponse.size());
		if(openSearchResponse.isEmpty()) {
			performanceMillis = System.currentTimeMillis();
			BaseResponseDTO response = new BaseResponseDTO();
			response.setMessage(ResponseConstants.NO_DOCUMENT_FOUND);
			log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
        	log.info(LoggingConstants.ANONYMIZED_RETRIEVE_PROCESS_END,
					(System.currentTimeMillis() - serviceStartTime) + Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
        	return response;
		}
		performanceMillis = System.currentTimeMillis();
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(openSearchResponse, GenericConstants.LOG_FILE_NAME, GenericConstants.ZIP_ARCHIVE_NAME);
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info(LoggingConstants.ANONYMIZED_RETRIEVE_PROCESS_END,
				(System.currentTimeMillis() - serviceStartTime) + Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
		return response;
	}

	@Override
	public BaseResponseDTO getMonthlyNotifications(String ticketNumber, String referenceMonth, String endMonth, String publicAuthorityName) throws IOException, ParseException,CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, LogExtractorException {
		log.info("Monthly notifications retrieve process - START - user={}, ticket number={}, reference month={}, end month={}, public authority name={}", MDC.get(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER), ticketNumber, referenceMonth, endMonth, publicAuthorityName);
		long serviceStartTime = System.currentTimeMillis();
		FileUtilities utils = new FileUtilities();
		List<File> csvFiles = new ArrayList<>();
		log.info("Getting public authority id...");
		long performanceMillis = System.currentTimeMillis();
		String encodedPublicAuthorityName = deanonimizationApiHandler.getPublicAuthorityId(publicAuthorityName);
        log.info("Public authority id retrieved in {} ms, getting notifications, publicAuthority={}, startDate={}, endDate={}", System.currentTimeMillis() - performanceMillis, encodedPublicAuthorityName, referenceMonth, endMonth);
        performanceMillis = System.currentTimeMillis();
		List<NotificationData> notifications = notificationApiHandler.getNotificationsByMonthsPeriod(referenceMonth, endMonth, 
				encodedPublicAuthorityName);
		log.info("{} notifications retrieved in {} ms, constructing service response...", notifications.size(), System.currentTimeMillis() - performanceMillis);
		if(notifications.isEmpty()) {
			performanceMillis = System.currentTimeMillis();
			BaseResponseDTO response = new BaseResponseDTO();
			response.setMessage(ResponseConstants.NO_NOTIFICATION_FOUND);
			log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
        	log.info("Monthly notifications retrieve process - END in {} ms",
					(System.currentTimeMillis() - serviceStartTime) + Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
        	return response;
		}
		performanceMillis = System.currentTimeMillis();
		int numberOfFiles = (int)Math.ceil(((double)notifications.size())/ GenericConstants.CSV_FILE_MAX_ROWS);
		int notificationPlaceholder = 0;
		while(numberOfFiles > 0) {
			List<NotificationData> notificationsPartition;
			if(numberOfFiles == 1) {
				notificationsPartition = notifications.subList(notificationPlaceholder, notifications.size());
			}
			else {
				notificationsPartition = notifications.subList(notificationPlaceholder,
						notificationPlaceholder+ GenericConstants.CSV_FILE_MAX_ROWS);
				notificationPlaceholder += GenericConstants.CSV_FILE_MAX_ROWS;
			}
			File file = utils.getFile(GenericConstants.NOTIFICATION_CSV_FILE_NAME, GenericConstants.CSV_EXTENSION);
			utils.writeCsv(file, utils.toCsv(notificationsPartition));
			csvFiles.add(file);
			numberOfFiles--;
		}
		DownloadArchiveResponseDto response = ResponseConstructor.createCsvFileResponse(csvFiles, GenericConstants.ZIP_ARCHIVE_NAME);
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info("Monthly notifications retrieve process - END in {} ms",
				(System.currentTimeMillis() - serviceStartTime) + Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
		return response;
	}
	
	@Override
	public BaseResponseDTO getTraceIdLogs(String dateFrom, String dateTo, String traceId) throws IOException, LogExtractorException {
		log.info("Anonymized logs retrieve process - START - user={}, traceId={}, startDate={}, endDate={}",
				MDC.get(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER), traceId, dateFrom, dateTo);
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting anonymized logs...");
		List<String> openSearchResponse = openSearchApiHandler.getAnonymizedLogsByTraceId(traceId, dateFrom, dateTo);
		long performanceMillis = System.currentTimeMillis() - serviceStartTime;
		log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, performanceMillis, openSearchResponse.size());
		if(openSearchResponse.isEmpty()) {
			performanceMillis = System.currentTimeMillis();
			BaseResponseDTO response = new BaseResponseDTO();
			response.setMessage(ResponseConstants.NO_DOCUMENT_FOUND);
			log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
        	log.info(LoggingConstants.ANONYMIZED_RETRIEVE_PROCESS_END, performanceMillis +
					Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
        	return response;
		}
		performanceMillis = System.currentTimeMillis();
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(openSearchResponse,
				GenericConstants.LOG_FILE_NAME, GenericConstants.ZIP_ARCHIVE_NAME);
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info(LoggingConstants.ANONYMIZED_RETRIEVE_PROCESS_END, performanceMillis +
				Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
		return response;
	}
	
	@Override
	public BaseResponseDTO getNotificationInfoLogs(String ticketNumber, String iun) throws IOException, InterruptedException, LogExtractorException {
		log.info("Notification data retrieve process - START - user={}, ticket number={}, iun={}", MDC.get(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER), ticketNumber, iun);
		ArrayList<String> downloadKeys = new ArrayList<>();
		ArrayList<String> downloadUrls = new ArrayList<>();
		long serviceStartTime = System.currentTimeMillis();
		double secondsToWait = 0;
		ObjectMapper mapper = new ObjectMapper();
		FileUtilities utils = new FileUtilities();
		ArrayList<File> filesToAdd = new ArrayList<>();
		log.info(LoggingConstants.GET_NOTIFICATION_DETAILS);
		NotificationDetailsResponseDto notificationDetails = notificationApiHandler.getNotificationDetails(iun);
		OffsetDateTime notificationStartDate = OffsetDateTime.parse(notificationDetails.getSentAt());
		String notificationEndDate = notificationStartDate.plusMonths(3).toString();
		log.info("Service response: notificationDetails={} retrieved in {} ms, getting history data...", mapper.writer().writeValueAsString(notificationDetails), System.currentTimeMillis() - serviceStartTime);
		NotificationHistoryResponseDto notificationHistory = notificationApiHandler.getNotificationHistory(iun, notificationDetails.getRecipients().size(), notificationStartDate.toString());
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
        	FileDownloadMetadataResponseDto downloadData = notificationApiHandler.getDownloadMetadata(key);
        	downloadUrls.add(downloadData.getDownload().getUrl());
        	if(null != downloadData && null != downloadData.getDownload() 
        			&& null == downloadData.getDownload().getUrl() && null != downloadData.getDownload().getRetryAfter()
        			&& secondsToWait < downloadData.getDownload().getRetryAfter()) {
        		secondsToWait = downloadData.getDownload().getRetryAfter();
        	}
        }
        if(secondsToWait > 0) {
        	log.info("Notification downloads' metadata retrieved in {} ms, physical files aren't ready yet. Constructing service response...", System.currentTimeMillis() - performanceMillis);
			performanceMillis = System.currentTimeMillis();
			int timeToWaitInMinutes = (int)Math.ceil(secondsToWait/60);
        	BaseResponseDTO response = new BaseResponseDTO();
			response.setMessage(ResponseConstants.OPERATION_CANNOT_BE_COMPLETED_MESSAGE + timeToWaitInMinutes +
					(timeToWaitInMinutes > 1 ? GenericConstants.MINUTES_LABEL : GenericConstants.MINUTE_LABEL));
			log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
			log.info("Notification data retrieve process - END in {} ms",
					(System.currentTimeMillis() - serviceStartTime) + Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
        	return response;
        }
        else {
        	log.info("Notification downloads' metadata retrieved in {} ms, getting physical files... ", System.currentTimeMillis() - performanceMillis);
        	performanceMillis = System.currentTimeMillis();
        	for(String url : downloadUrls) {
        		byte[] externalFile = notificationApiHandler.getFile(url);
        		File downloadedFile = utils.getFile(GenericConstants.DOCUMENT_LABEL, GenericConstants.PDF_EXTENSION);
        		FileUtils.writeByteArrayToFile(downloadedFile, externalFile);
        		filesToAdd.add(downloadedFile);
        	}
        	log.info("Physical files retrieved in {} ms", System.currentTimeMillis() - performanceMillis);
        	List<String> openSearchResponse = openSearchApiHandler.getAnonymizedLogsByIun(iun, notificationStartDate.toString(), notificationEndDate);
    		log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, System.currentTimeMillis() - performanceMillis, openSearchResponse.size());
			performanceMillis = System.currentTimeMillis();
			DownloadArchiveResponseDto response = ResponseConstructor.createNotificationLogResponse(openSearchResponse, filesToAdd, GenericConstants.LOG_FILE_NAME, GenericConstants.ZIP_ARCHIVE_NAME);
			log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
			log.info("Notification data retrieve process - END in {} ms",
					(System.currentTimeMillis() - serviceStartTime) + Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
    		return response;
        }
	}
		
	public BaseResponseDTO getDeanonimizedPersonLogs(RecipientTypes recipientType, String dateFrom, String dateTo, String ticketNumber, String taxid, String iun) throws IOException, LogExtractorException {
		log.info("Deanonimized logs retrieve process - START - user={}, ticket number={}, taxId={}, startDate={}, endDate={}, iun={}", MDC.get(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER), ticketNumber, taxid, dateFrom, dateTo, iun);
		long serviceStartTime = System.currentTimeMillis();
		List<String> openSearchResponse;
		long performanceMillis = 0;
		List<String> deanonimizedOpenSearchResponse = new ArrayList<>();
		//use case 3
		if (dateFrom != null && dateTo != null && taxid != null && recipientType!=null && ticketNumber!=null && iun==null) {
			log.info("Getting internal id...");
			String internalId = deanonimizationApiHandler.getUniqueIdentifierForPerson(recipientType, taxid);
			log.info("Service response: internalId={} retrieved in {} ms", internalId, System.currentTimeMillis() - serviceStartTime);
			performanceMillis = System.currentTimeMillis();
			openSearchResponse = openSearchApiHandler.getAnonymizedLogsByUid(internalId, dateFrom, dateTo);
			log.info("Query execution completed in {} ms, retrieved {} documents, deanonimizing results...",
					System.currentTimeMillis() - performanceMillis, openSearchResponse.size());
			performanceMillis = System.currentTimeMillis();
			deanonimizedOpenSearchResponse = deanonimizationApiHandler.deanonimizeDocuments(openSearchResponse, recipientType);
		} else{
			//use case 4
			if (iun!=null && ticketNumber!=null) {
				log.info(LoggingConstants.GET_NOTIFICATION_DETAILS);
				NotificationDetailsResponseDto notificationDetails = notificationApiHandler.getNotificationDetails(iun);
				log.info("Service response: notificationDetails={} retrieved in {} ms", new ObjectMapper().writer().writeValueAsString(notificationDetails), System.currentTimeMillis() - serviceStartTime);
				OffsetDateTime notificationStartDate = OffsetDateTime.parse(notificationDetails.getSentAt());
				String notificationEndDate = notificationStartDate.plusMonths(3).toString();
				performanceMillis = System.currentTimeMillis();
				openSearchResponse = openSearchApiHandler.getAnonymizedLogsByIun(iun, notificationStartDate.toString(), notificationEndDate);
				log.info("Query execution completed in {} ms, retrieved {} documents, deanonimizing results...",
						System.currentTimeMillis() - performanceMillis, openSearchResponse.size());
				performanceMillis = System.currentTimeMillis();
				deanonimizedOpenSearchResponse = deanonimizationApiHandler.deanonimizeDocuments(openSearchResponse, RecipientTypes.PF);
			}
		}
		log.info("Deanonimization completed in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
		if(deanonimizedOpenSearchResponse.isEmpty()) {
			performanceMillis = System.currentTimeMillis();
			BaseResponseDTO response = new BaseResponseDTO();
			response.setMessage(ResponseConstants.NO_DOCUMENT_FOUND);
			log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
			log.info("Deanonimized logs retrieve process - END in {} ms",
					(System.currentTimeMillis() - serviceStartTime) + Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
			return response;
		}
		performanceMillis = System.currentTimeMillis();
		DownloadArchiveResponseDto response = ResponseConstructor.createSimpleLogResponse(deanonimizedOpenSearchResponse, GenericConstants.LOG_FILE_NAME, GenericConstants.ZIP_ARCHIVE_NAME);
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info("deanonimized logs retrieve process - END in {} ms",
				(System.currentTimeMillis() - serviceStartTime) + Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
		return response;
	}
}
