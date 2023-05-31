package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.exception.CustomException;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.SessionLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiHandlerFactory;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationService;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;
import it.gov.pagopa.logextractor.util.external.s3.S3ClientService;
import it.gov.pagopa.logextractor.util.external.s3.S3DocumentDownloader;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation class of {@link LogService}
 */
@Service
@Slf4j
public class LogServiceImpl implements LogService {

	private static final String OS_RESULT = "dati";

	@Value("${external.s3.saml.assertion.bucket}")
	String s3Bucket;

	@Autowired
	AmazonS3 s3Client;
	
	@Autowired
	NotificationApiHandler notificationApiHandler;
	
	@Autowired
	OpenSearchApiHandlerFactory openSearchApiHandlerFactory;
	
	@Autowired
	DeanonimizationService deanonimizationService;
	
	@Autowired
	ZipService zipService;
	
	@Autowired
	FileUtilities fileUtils;
	
	@Autowired 
	S3ClientService s3ClientService;

	@Autowired 
	NotificationLogService notificationLogService;

	@Autowired 
	S3DocumentDownloader s3DocumentDownloader;

	@Override
	@Async
	public String getAnonymizedPersonLogs(String key, String pass, PersonLogsRequestDto requestData, String xPagopaHelpdUid, String xPagopaCxType)
			throws IOException {
		log.info(
				"Anonymized logs retrieve process - START - user={}, userType={}, ticketNumber={}, "
						+ "internalId={}, startDate={}, endDate={}, iun={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(), requestData.getPersonId(),
				requestData.getDateFrom(), requestData.getDateTo(), requestData.getIun());
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
		int docCount = 0;

		ZipInfo zipInfo = zipService.createZip(key, pass, s3ClientService.uploadStream(key));
		try {
			zipService.addEntry(zipInfo, OS_RESULT + GenericConstants.TXT_EXTENSION);
			// use case 7
			if (requestData.getDateFrom() != null && requestData.getDateTo() != null && requestData.getPersonId() != null
					&& requestData.getIun() == null) {
				log.info("Getting activities' anonymized history... ");
				performanceMillis = System.currentTimeMillis();
	
				docCount = openSearchApiHandlerFactory.getOpenSearchApiHanlder().getAnonymizedLogsByUid(requestData.getPersonId(), requestData.getDateFrom(),
						requestData.getDateTo(), zipInfo.getZos());
			} else {
				// use case 8
				if (requestData.getIun() != null) {
					log.info(LoggingConstants.GET_NOTIFICATION_DETAILS);
					NotificationDetailsResponseDto notificationDetails = notificationApiHandler
							.getNotificationDetails(requestData.getIun());
					log.info("Service response: notificationDetails={} retrieved in {} ms",
							new ObjectMapper().writer().writeValueAsString(notificationDetails),
							System.currentTimeMillis() - serviceStartTime);
					OffsetDateTime notificationStartDate = OffsetDateTime.parse(notificationDetails.getSentAt());
					String notificationEndDate = notificationStartDate.plusMonths(3).toString();
					performanceMillis = System.currentTimeMillis();
					docCount = openSearchApiHandlerFactory.getOpenSearchApiHanlder().getAnonymizedLogsByIun(requestData.getIun(),
							notificationStartDate.toString(), notificationEndDate, zipInfo.getZos());
				}
			}
			zipService.closeEntry(zipInfo);
			log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, System.currentTimeMillis() - performanceMillis,
					docCount);
			if (docCount == 0) {
				throw new CustomException(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE, 204);
			}
		}catch(Exception err) {
			log.error("Error preparing zip file", err);
			zipService.addEntryWithContent(zipInfo, "error.txt", err.getMessage());
		}
		zipService.close(zipInfo);
		log.info(LoggingConstants.ANONYMIZED_RETRIEVE_PROCESS_END, (System.currentTimeMillis() - serviceStartTime));
		return zipInfo.getPassword();
	}

	@Override
	@Async
	public void getMonthlyNotifications(String key, String zipPassword, MonthlyNotificationsRequestDto requestData, String xPagopaHelpdUid,
			String xPagopaCxType)
			throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, LogExtractorException {
		log.info(
				"Monthly notifications retrieve process - START - user={},"
						+ "userType={}, ticketNumber={}, referenceMonth={}, endMonth={}, publicAuthorityName={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(), requestData.getReferenceMonth(),
				requestData.getEndMonth(), requestData.getPublicAuthorityName());
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting public authority id...");
		ZipInfo zipInfo = zipService.createZip(key, zipPassword, s3ClientService.uploadStream(key));
		long performanceMillis = System.currentTimeMillis();

		try {
		String encodedPublicAuthorityName = deanonimizationService.getPublicAuthorityId(requestData.getPublicAuthorityName());
		log.info(
				"Public authority id retrieved in {} ms, getting notifications, publicAuthority={}, startDate={}, "
						+ "endDate={}",
				System.currentTimeMillis() - performanceMillis, encodedPublicAuthorityName,
				requestData.getReferenceMonth(), requestData.getEndMonth());
		performanceMillis = System.currentTimeMillis();
		List<NotificationData> notifications = notificationApiHandler.getNotificationsByMonthsPeriod(
				requestData.getReferenceMonth(), requestData.getEndMonth(), encodedPublicAuthorityName);
		log.info("{} notifications retrieved in {} ms, constructing service response...", notifications.size(),
				System.currentTimeMillis() - performanceMillis);
		if (notifications.isEmpty()) {
			throw new CustomException(ResponseConstants.NO_NOTIFICATION_FOUND_MESSAGE, 204);
		}
		performanceMillis = System.currentTimeMillis();
		int numberOfFiles = (int) Math.ceil(((double) notifications.size()) / GenericConstants.CSV_FILE_MAX_ROWS);
		int notificationPlaceholder = 0;
		while (numberOfFiles > 0) {
			List<NotificationData> notificationsPartition;
			if (numberOfFiles == 1) {
				notificationsPartition = notifications.subList(notificationPlaceholder, notifications.size());
			} else {
				notificationsPartition = notifications.subList(notificationPlaceholder,
						notificationPlaceholder + GenericConstants.CSV_FILE_MAX_ROWS);
				notificationPlaceholder += GenericConstants.CSV_FILE_MAX_ROWS;
			}
			zipService.addEntry(zipInfo, GenericConstants.NOTIFICATION_CSV_FILE_NAME + GenericConstants.CSV_EXTENSION);
			fileUtils.writeCsv(fileUtils.toCsv(notificationsPartition), zipInfo.getZos());
			zipService.closeEntry(zipInfo);
			numberOfFiles--;
		}
		}catch(Exception err) {
			log.error("Error preparing zip file", err);
			zipService.addEntryWithContent(zipInfo, "error.txt", err.getMessage());
		}
		zipService.close(zipInfo);
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info("Monthly notifications retrieve process - END in {} ms",
				(System.currentTimeMillis() - serviceStartTime));
	}

	@Override
	@Async
	public void getTraceIdLogs(String key, String zipPassword, TraceIdLogsRequestDto requestData, String xPagopaHelpdUid, String xPagopaCxType)
			throws IOException {
		log.info(
				"Anonymized logs retrieve process - START - user={}, userType={},"
						+ " traceId={}, startDate={}, endDate={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTraceId(), requestData.getDateFrom(),
				requestData.getDateTo());
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting anonymized logs...");

		ZipInfo zipInfo = zipService.createZip(key, zipPassword, s3ClientService.uploadStream(key));
		try {
			zipService.addEntry(zipInfo, OS_RESULT + GenericConstants.TXT_EXTENSION);
			int docCount = openSearchApiHandlerFactory.getOpenSearchApiHanlder().getAnonymizedLogsByTraceId(requestData.getTraceId(),
					requestData.getDateFrom(), requestData.getDateTo(), zipInfo.getZos());
			zipService.closeEntry(zipInfo);
			long performanceMillis = System.currentTimeMillis() - serviceStartTime;
			log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, performanceMillis, docCount);
			if (docCount == 0) {
				throw new CustomException(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE, 204);
			}
		}catch(Exception err) {
			log.error("Error processing NotificationLog Request", err);
			zipService.addEntryWithContent(zipInfo, "error.txt", err.getMessage());
		}
		zipService.close(zipInfo);		
		log.info(LoggingConstants.ANONYMIZED_RETRIEVE_PROCESS_END, (System.currentTimeMillis() - serviceStartTime));
	}

	@Override
	@Async
	public void getNotificationInfoLogs(String key, String zipPassword, NotificationInfoRequestDto requestData, String xPagopaHelpdUid,
			String xPagopaCxType) throws IOException {
		notificationLogService.getNotificationInfoLogs(key, zipPassword, requestData, xPagopaHelpdUid, xPagopaCxType);
	}

	
	@Async
	public void getDeanonimizedPersonLogs(String key, String zipPassword, PersonLogsRequestDto requestData, String xPagopaHelpdUid,
			String xPagopaCxType) throws IOException, LogExtractorException {
		log.info(
				"Deanonimized logs retrieve process - START - user={}, userType={}, ticketNumber={}, taxId={}, "
						+ "startDate={}, endDate={}, iun={}, recipientType={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(), requestData.getTaxId(),
				requestData.getDateFrom(), requestData.getDateTo(), requestData.getIun(),
				requestData.getRecipientType());
		long serviceStartTime = System.currentTimeMillis();
		int docCount = 0;
		long performanceMillis = 0;
		File tmp = fileUtils.getFileWithRandomName(OS_RESULT, GenericConstants.TXT_EXTENSION);
		ZipInfo zipInfo = zipService.createZip(key, zipPassword, s3ClientService.uploadStream(key));
		
		try (OutputStream tmpOutStream = new FileOutputStream(tmp)){
	
			// use case 3
			if (requestData.getDateFrom() != null && requestData.getDateTo() != null && requestData.getTaxId() != null
					&& requestData.getRecipientType() != null && requestData.getIun() == null) {
				log.info("Getting internal id...");
				String internalId = deanonimizationService.getUniqueIdentifierForPerson(requestData.getRecipientType(),
						requestData.getTaxId());
				log.info("Service response: internalId={} retrieved in {} ms", internalId,
						System.currentTimeMillis() - serviceStartTime);
				performanceMillis = System.currentTimeMillis();
	
				FilenameCollector filenameCollector = new FilenameCollector();
				docCount = openSearchApiHandlerFactory.getOpenSearchApiHanlder(filenameCollector).getAnonymizedLogsByUid(internalId, requestData.getDateFrom(),
						requestData.getDateTo(), tmpOutStream);
				log.info(LoggingConstants.QEURY_EXECUTION_COMPLETED_TIME_DEANONIMIZE_DOCS,
						System.currentTimeMillis() - performanceMillis, docCount);
				tmpOutStream.flush();
				performanceMillis = System.currentTimeMillis();
				zipService.addEntry(zipInfo, OS_RESULT + GenericConstants.TXT_EXTENSION);
				deanonimizationService.deanonimizeDocuments(tmp, requestData.getRecipientType(),
						zipInfo.getZos());
				zipService.closeEntry(zipInfo);
	
				s3DocumentDownloader.downloadToZip(s3Bucket, filenameCollector.getNames(), zipInfo);
			} else {
				if (requestData.getIun() != null) {
					// use case 4
					log.info(LoggingConstants.GET_NOTIFICATION_DETAILS);
					NotificationDetailsResponseDto notificationDetails = notificationApiHandler
							.getNotificationDetails(requestData.getIun());
					log.info("Service response: notificationDetails={} retrieved in {} ms",
							new ObjectMapper().writer().writeValueAsString(notificationDetails),
							System.currentTimeMillis() - serviceStartTime);
					OffsetDateTime notificationStartDate = OffsetDateTime.parse(notificationDetails.getSentAt());
					String notificationEndDate = notificationStartDate.plusMonths(3).toString();
					performanceMillis = System.currentTimeMillis();
					docCount = openSearchApiHandlerFactory.getOpenSearchApiHanlder().getAnonymizedLogsByIun(requestData.getIun(),
							notificationStartDate.toString(), notificationEndDate, tmpOutStream);
					tmpOutStream.flush();
					log.info(LoggingConstants.QEURY_EXECUTION_COMPLETED_TIME_DEANONIMIZE_DOCS,
							System.currentTimeMillis() - performanceMillis, docCount);
					performanceMillis = System.currentTimeMillis();
					zipService.addEntry(zipInfo, OS_RESULT + GenericConstants.TXT_EXTENSION);
					deanonimizationService.deanonimizeDocuments(tmp, RecipientTypes.PF, zipInfo.getZos());
					zipService.closeEntry(zipInfo);
				}
			}
			zipService.close(zipInfo);
		}
		Files.delete(tmp.toPath());
		log.info("Deanonimization completed in {} ms, constructing service response...",
				System.currentTimeMillis() - performanceMillis);
		log.info("deanonimized logs retrieve process - END in {} ms", (System.currentTimeMillis() - serviceStartTime));
		if (docCount == 0) {
			throw new CustomException(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE, 204);
		}
		
	}

	@Override
	@Async
	public void getAnonymizedSessionLogs(String key, String zipPassword,SessionLogsRequestDto requestData, String xPagopaHelpdUid,
			String xPagopaCxType) throws IOException {
		log.info(
				"Anonymized session logs retrieve process - START - user={}, userType={},"
						+ " ticketNumber={}, jti={}, startDate={}, endDate={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(), requestData.getJti(),
				requestData.getDateFrom(), requestData.getDateTo());


		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = System.currentTimeMillis();
		ZipInfo zipInfo = zipService.createZip(key, zipPassword, s3ClientService.uploadStream(key));
		try {
			int docCount = 0;
			log.info("Getting session activities' anonymized history... ");
			performanceMillis = System.currentTimeMillis();
	
			zipService.addEntry(zipInfo, OS_RESULT + GenericConstants.TXT_EXTENSION);
			docCount = openSearchApiHandlerFactory.getOpenSearchApiHanlder().getAnonymizedSessionLogsByJti(requestData.getJti(), requestData.getDateFrom(),
					requestData.getDateTo(), zipInfo.getZos());
			zipService.closeEntry(zipInfo);
			log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, System.currentTimeMillis() - performanceMillis,
					docCount);
			performanceMillis = System.currentTimeMillis();
			if (docCount == 0) {
				throw new CustomException(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE, 204);
			}
		}catch(Exception err) {
			log.error("Error processing NotificationLog Request", err);
			zipService.addEntryWithContent(zipInfo, "error.txt", err.getMessage());
		}
		zipService.close(zipInfo);		
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info(LoggingConstants.ANONYMIZED_RETRIEVE_PROCESS_END, (System.currentTimeMillis() - serviceStartTime));
	}

	@Override
	@Async
	public void getDeanonimizedSessionLogs(String key, String zipPassword,SessionLogsRequestDto requestData,
													  String xPagopaHelpdUid,
													  String xPagopaCxType) throws IOException, LogExtractorException {
		log.info("Deanonimized session logs retrieve process - START - user={}, userType={}, ticketNumber={}, " +
				"jti={}, startDate={}, endDate={}", xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(),
				requestData.getJti(), requestData.getDateFrom(), requestData.getDateTo());
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
		File openSearchResponse = new FileUtilities().getFileWithRandomName(OS_RESULT, GenericConstants.TXT_EXTENSION);
		FileOutputStream tmpOutStream = new FileOutputStream(openSearchResponse);
		int docCount = 0;
		ZipInfo zipInfo = zipService.createZip(key, zipPassword, s3ClientService.uploadStream(key));
		try {
			log.info("Getting session activities' deanonimized history... ");
			performanceMillis = System.currentTimeMillis();
			FilenameCollector filenameCollector = new FilenameCollector();
			docCount = openSearchApiHandlerFactory.getOpenSearchApiHanlder(filenameCollector).getAnonymizedSessionLogsByJti(requestData.getJti(), requestData.getDateFrom(), requestData.getDateTo(), tmpOutStream);
	
			log.info("Query execution completed in {} ms, retrieved {} documents, deanonimizing results...",
					System.currentTimeMillis() - performanceMillis, docCount);
			tmpOutStream.flush();
			IOUtils.closeQuietly(tmpOutStream);
			zipService.addEntry(zipInfo, OS_RESULT+GenericConstants.TXT_EXTENSION);
			deanonimizationService.deanonimizeDocuments(openSearchResponse, RecipientTypes.PF, zipInfo.getZos());
			zipService.closeEntry(zipInfo);
			
			s3DocumentDownloader.downloadToZip(s3Bucket, filenameCollector.getNames(), zipInfo);
	
			Files.delete(openSearchResponse.toPath());
			log.info("Deanonimization completed in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
			performanceMillis = System.currentTimeMillis();
			if(docCount == 0) {
				throw new CustomException(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE, 204);
			}
		}catch(Exception err) {
			log.error("Error processing NotificationLog Request", err);
			zipService.addEntryWithContent(zipInfo, "error.txt", err.getMessage());
		}
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info(LoggingConstants.DEANONIMIZED_RETRIEVE_PROCESS_END, (System.currentTimeMillis() - serviceStartTime));
	}
	
	
	public BaseResponseDto getCurrentProcessStatus(String key) {
		BaseResponseDto ret = new BaseResponseDto();
		try{
			s3ClientService.getObject(key);
			ret.setMessage(s3ClientService.downloadUrl(key));
		}catch(Exception err) {
			ret.setMessage("NotReady");
		}
		return ret;
	}
}
