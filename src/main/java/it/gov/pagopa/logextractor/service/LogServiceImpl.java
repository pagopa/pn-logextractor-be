package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.dto.response.FileDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationHistoryResponseDto;
import it.gov.pagopa.logextractor.exception.CustomException;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.SessionLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.external.opensearch.S3DocumentDownloader;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationService;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationDownloadFileData;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;

/**
 * Implementation class of {@link LogService}
 */
@Service
@Slf4j
public class LogServiceImpl implements LogService {

	private static final String OS_RESULT = "dati";

	@Value("${external.s3.saml.assertion.region}")
	String s3Region;
	@Value("${external.s3.saml.assertion.bucket}")
	String s3Bucket;
	@Value("${external.s3.saml.assertion.awsprofile:}")
	String awsProfile;

	@Autowired
	NotificationApiHandler notificationApiHandler;
	
	@Autowired
	OpenSearchApiHandler openSearchApiHandler;
	
	@Autowired
	DeanonimizationApiHandler deanonimizationApiHandler;
	
	@Autowired
	ThreadLocalOutputStreamService threadLocalService;
	
	@Autowired
	FileUtilities fileUtils;

	@Override
	public void getAnonymizedPersonLogs(PersonLogsRequestDto requestData, String xPagopaHelpdUid, String xPagopaCxType)
			throws IOException {
		log.info(
				"Anonymized logs retrieve process - START - user={}, userType={}, ticketNumber={}, "
						+ "internalId={}, startDate={}, endDate={}, iun={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(), requestData.getPersonId(),
				requestData.getDateFrom(), requestData.getDateTo(), requestData.getIun());
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
		int docCount = 0;

		ZipOutputStream zos = threadLocalService.get();
		threadLocalService.addEntry(OS_RESULT + GenericConstants.TXT_EXTENSION);
		// use case 7
		if (requestData.getDateFrom() != null && requestData.getDateTo() != null && requestData.getPersonId() != null
				&& requestData.getIun() == null) {
			log.info("Getting activities' anonymized history... ");
			performanceMillis = System.currentTimeMillis();

			docCount = openSearchApiHandler.getAnonymizedLogsByUid(requestData.getPersonId(), requestData.getDateFrom(),
					requestData.getDateTo(), zos);
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
				docCount = openSearchApiHandler.getAnonymizedLogsByIun(requestData.getIun(),
						notificationStartDate.toString(), notificationEndDate, zos);
			}
		}
		threadLocalService.closeEntry();
		log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, System.currentTimeMillis() - performanceMillis,
				docCount);
		if (docCount == 0) {
			throw new CustomException(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE, 204);
		}
		performanceMillis = System.currentTimeMillis();
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info(LoggingConstants.ANONYMIZED_RETRIEVE_PROCESS_END, (System.currentTimeMillis() - serviceStartTime));
	}

	@Override
	public void getMonthlyNotifications(MonthlyNotificationsRequestDto requestData, String xPagopaHelpdUid,
			String xPagopaCxType)
			throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, LogExtractorException {
		log.info(
				"Monthly notifications retrieve process - START - user={},"
						+ "userType={}, ticketNumber={}, referenceMonth={}, endMonth={}, publicAuthorityName={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(), requestData.getReferenceMonth(),
				requestData.getEndMonth(), requestData.getPublicAuthorityName());
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting public authority id...");
		long performanceMillis = System.currentTimeMillis();
		String encodedPublicAuthorityName = deanonimizationApiHandler.getPublicAuthorityId(requestData.getPublicAuthorityName());
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
			threadLocalService.addEntry(GenericConstants.NOTIFICATION_CSV_FILE_NAME + GenericConstants.CSV_EXTENSION);
			fileUtils.writeCsv(fileUtils.toCsv(notificationsPartition), threadLocalService.get());
			threadLocalService.closeEntry();
			numberOfFiles--;
		}
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info("Monthly notifications retrieve process - END in {} ms",
				(System.currentTimeMillis() - serviceStartTime));
	}

	@Override
	public void getTraceIdLogs(TraceIdLogsRequestDto requestData, String xPagopaHelpdUid, String xPagopaCxType)
			throws IOException {
		log.info(
				"Anonymized logs retrieve process - START - user={}, userType={},"
						+ " traceId={}, startDate={}, endDate={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTraceId(), requestData.getDateFrom(),
				requestData.getDateTo());
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting anonymized logs...");

		OutputStream out = threadLocalService.get();
		threadLocalService.addEntry(OS_RESULT + GenericConstants.TXT_EXTENSION);
		int docCount = openSearchApiHandler.getAnonymizedLogsByTraceId(requestData.getTraceId(),
				requestData.getDateFrom(), requestData.getDateTo(), out);
		threadLocalService.closeEntry();
		long performanceMillis = System.currentTimeMillis() - serviceStartTime;
		log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, performanceMillis, docCount);
		if (docCount == 0) {
			throw new CustomException(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE, 204);
		}
		performanceMillis = System.currentTimeMillis();
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info(LoggingConstants.ANONYMIZED_RETRIEVE_PROCESS_END, (System.currentTimeMillis() - serviceStartTime));
	}

	@Override
	public void getNotificationInfoLogs(NotificationInfoRequestDto requestData, String xPagopaHelpdUid,
			String xPagopaCxType) throws IOException {
		log.info("Notification data retrieve process - START - user={}, userType={}, ticketNumber={}, iun={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(), requestData.getIun());
		ArrayList<NotificationDownloadFileData> downloadableFiles = new ArrayList<>();
		long serviceStartTime = System.currentTimeMillis();
		double secondsToWait = 0;
		ObjectMapper mapper = new ObjectMapper();
		ArrayList<File> filesToAdd = new ArrayList<>();
		log.info(LoggingConstants.GET_NOTIFICATION_DETAILS);
		NotificationDetailsResponseDto notificationDetails = notificationApiHandler
				.getNotificationDetails(requestData.getIun());
		OffsetDateTime notificationStartDate = OffsetDateTime.parse(notificationDetails.getSentAt());
		String notificationEndDate = notificationStartDate.plusMonths(3).toString();
		log.info("Service response: notificationDetails={} retrieved in {} ms, getting history data...",
				mapper.writer().writeValueAsString(notificationDetails), System.currentTimeMillis() - serviceStartTime);
		NotificationHistoryResponseDto notificationHistory = notificationApiHandler.getNotificationHistory(
				requestData.getIun(), notificationDetails.getRecipients().size(), notificationStartDate.toString());
		log.info("Service response: notificationHistory={} retrieved in {} ms, getting legal facts' keys...",
				mapper.writer().writeValueAsString(notificationHistory), System.currentTimeMillis() - serviceStartTime);
		long performanceMillis = System.currentTimeMillis();
		ArrayList<NotificationDownloadFileData> downloadFileData = new ArrayList<>(
				notificationApiHandler.getLegalFactFileDownloadData(notificationHistory));
		log.info("Legal facts' keys retrieved in {} ms, getting notification documents' keys...",
				System.currentTimeMillis() - performanceMillis);
		performanceMillis = System.currentTimeMillis();
		downloadFileData.addAll(notificationApiHandler.getNotificationDocumentFileDownloadData(notificationDetails));
		log.info("Notification documents' keys retrieved in {} ms, getting payment documents' keys...",
				System.currentTimeMillis() - performanceMillis);
		performanceMillis = System.currentTimeMillis();
		downloadFileData.addAll(notificationApiHandler.getPaymentFilesDownloadData(notificationDetails));
		log.info("Notification payment' keys retrieved in {} ms, getting downloads' metadata...",
				System.currentTimeMillis() - performanceMillis);
		performanceMillis = System.currentTimeMillis();
		List<NotificationDownloadFileData> filesNotDownloadable = new ArrayList<>();
		for (NotificationDownloadFileData currentDownloadData : downloadFileData) {
			try {
				FileDownloadMetadataResponseDto downloadMetaData = notificationApiHandler
						.getDownloadMetadata(currentDownloadData.getKey());
				currentDownloadData.setDownloadUrl(downloadMetaData.getDownload().getUrl());
				downloadableFiles.add(currentDownloadData);
				if ( secondsToWait < getRetryAfter(downloadMetaData)) {
					secondsToWait = downloadMetaData.getDownload().getRetryAfter();
				}
			} catch (HttpServerErrorException | HttpClientErrorException ex) {
				filesNotDownloadable.add(currentDownloadData);
			}
		}
		if (secondsToWait > 0) {
			log.info(
					"Notification downloads' metadata retrieved in {} ms, physical files aren't ready yet. Constructing service response...",
					System.currentTimeMillis() - performanceMillis);
			int timeToWaitInMinutes = (int) Math.ceil(secondsToWait / 60);
			throw new CustomException(ResponseConstants.OPERATION_CANNOT_BE_COMPLETED_MESSAGE + timeToWaitInMinutes
					+ (timeToWaitInMinutes > 1 ? GenericConstants.MINUTES_LABEL : GenericConstants.MINUTE_LABEL), 503);
		} else {
			log.info("Notification downloads' metadata retrieved in {} ms, getting physical files... ",
					System.currentTimeMillis() - performanceMillis);
			performanceMillis = System.currentTimeMillis();
			for (NotificationDownloadFileData currentDownloadableFile : downloadableFiles) {
				File downloadedFile = fileUtils.getFileWithRandomName(
						currentDownloadableFile.getFileCategory() + "-" + currentDownloadableFile.getKey(),
						GenericConstants.PDF_EXTENSION);
				if (notificationApiHandler.downloadToFile(currentDownloadableFile.getDownloadUrl(),
						downloadedFile) > 0) {
					filesToAdd.add(downloadedFile);
				}
			}
			log.info("Physical files retrieved in {} ms", System.currentTimeMillis() - performanceMillis);

			OutputStream out = threadLocalService.get();
			threadLocalService.addEntry(OS_RESULT + GenericConstants.TXT_EXTENSION);
			int docsNumber = openSearchApiHandler.getAnonymizedLogsByIun(requestData.getIun(),
					notificationStartDate.toString(), notificationEndDate, out);
			threadLocalService.closeEntry();
			log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, System.currentTimeMillis() - performanceMillis,
					docsNumber);
			performanceMillis = System.currentTimeMillis();
			log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME,
					System.currentTimeMillis() - performanceMillis);
			log.info("Notification data retrieve process - END in {} ms",
					(System.currentTimeMillis() - serviceStartTime));
		}

		if (!filesNotDownloadable.isEmpty()) {
			threadLocalService.addEntry(GenericConstants.ERROR_SUMMARY_FILE_NAME + ".txt");
			JsonUtilities jsonUtilities = new JsonUtilities();
			String failsToString = jsonUtilities.toString(jsonUtilities.toJson(filesNotDownloadable));
			OutputStreamWriter osw = new OutputStreamWriter(threadLocalService.get());
			osw.write(failsToString);
			osw.flush();
			threadLocalService.closeEntry();
		}
	}

	private Integer getRetryAfter(FileDownloadMetadataResponseDto downloadMetaData) {
		if (downloadMetaData == null || downloadMetaData.getDownload() == null) {
			return 0;
		}else {
			Integer ret = downloadMetaData.getDownload().getRetryAfter();
			return ret==null?0:ret;
		}
	}
	
	public void getDeanonimizedPersonLogs(PersonLogsRequestDto requestData, String xPagopaHelpdUid,
			String xPagopaCxType) throws IOException, LogExtractorException {
		log.info(
				"Deanonimized logs retrieve process - START - user={}, userType={}, ticketNumber={}, taxId={}, "
						+ "startDate={}, endDate={}, iun={}, recipientType={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(), requestData.getTaxId(),
				requestData.getDateFrom(), requestData.getDateTo(), requestData.getIun(),
				requestData.getRecipientType());
		
		threadLocalService.addEntry("fakeEntry");
		threadLocalService.get().write("Solo per non far morire lo stream".getBytes());
		threadLocalService.closeEntry();
		threadLocalService.get().flush();
		long serviceStartTime = System.currentTimeMillis();
		int docCount = 0;
		long performanceMillis = 0;
//		File tmp = fileUtils.getFileWithRandomName(OS_RESULT, GenericConstants.TXT_EXTENSION);
		
		try (OutputStream tmpOutStream = OutputStream.nullOutputStream()){
	
			// use case 3
			if (requestData.getDateFrom() != null && requestData.getDateTo() != null && requestData.getTaxId() != null
					&& requestData.getRecipientType() != null && requestData.getIun() == null) {
				log.info("Getting internal id...");
				String internalId = deanonimizationApiHandler.getUniqueIdentifierForPerson(requestData.getRecipientType(),
						requestData.getTaxId());
				log.info("Service response: internalId={} retrieved in {} ms", internalId,
						System.currentTimeMillis() - serviceStartTime);
				performanceMillis = System.currentTimeMillis();
	
				S3DocumentDownloader s3 = new S3DocumentDownloader(awsProfile, s3Region, s3Bucket);
				openSearchApiHandler.setObserver(s3);
				openSearchApiHandler.addObserver(new DeanonimizationService(deanonimizationApiHandler, threadLocalService.get(), requestData.getRecipientType()));
				
				threadLocalService.addEntry(OS_RESULT + GenericConstants.TXT_EXTENSION);
				docCount = openSearchApiHandler.getAnonymizedLogsByUid(internalId, requestData.getDateFrom(),
						requestData.getDateTo(), tmpOutStream);
				log.info(LoggingConstants.QEURY_EXECUTION_COMPLETED_TIME_DEANONIMIZE_DOCS,
						System.currentTimeMillis() - performanceMillis, docCount);
				tmpOutStream.flush();
				performanceMillis = System.currentTimeMillis();
//				deanonimizationService.deanonimizeDocuments(tmp, requestData.getRecipientType(),
//						threadLocalService.get());
				threadLocalService.closeEntry();
	
				if (s3.getFileName() != null) {
					threadLocalService.addEntry(s3.getFileName(), s3.getFileContent());
				}
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
					openSearchApiHandler.setObserver(new DeanonimizationService(deanonimizationApiHandler, threadLocalService.get(), RecipientTypes.PF));

					threadLocalService.addEntry(OS_RESULT + GenericConstants.TXT_EXTENSION);
					docCount = openSearchApiHandler.getAnonymizedLogsByIun(requestData.getIun(),
							notificationStartDate.toString(), notificationEndDate, tmpOutStream);
					tmpOutStream.flush();
					log.info(LoggingConstants.QEURY_EXECUTION_COMPLETED_TIME_DEANONIMIZE_DOCS,
							System.currentTimeMillis() - performanceMillis, docCount);
					performanceMillis = System.currentTimeMillis();
//					deanonimizationService.deanonimizeDocuments(tmp, RecipientTypes.PF, threadLocalService.get());
					threadLocalService.closeEntry();
				}
			}
		}
//		Files.delete(tmp.toPath());
		log.info("Deanonimization completed in {} ms, constructing service response...",
				System.currentTimeMillis() - performanceMillis);
		log.info("deanonimized logs retrieve process - END in {} ms", (System.currentTimeMillis() - serviceStartTime));
		if (docCount == 0) {
			throw new CustomException(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE, 204);
		}
	}

	@Override
	public void getAnonymizedSessionLogs(SessionLogsRequestDto requestData, String xPagopaHelpdUid,
			String xPagopaCxType) throws IOException {
		log.info(
				"Anonymized session logs retrieve process - START - user={}, userType={},"
						+ " ticketNumber={}, jti={}, startDate={}, endDate={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(), requestData.getJti(),
				requestData.getDateFrom(), requestData.getDateTo());
		long serviceStartTime = System.currentTimeMillis();
		int docCount = 0;

		log.info("Getting session activities' anonymized history... ");
		long performanceMillis = System.currentTimeMillis();

		threadLocalService.addEntry(OS_RESULT + GenericConstants.TXT_EXTENSION);
		docCount = openSearchApiHandler.getAnonymizedSessionLogsByJti(requestData.getJti(), requestData.getDateFrom(),
				requestData.getDateTo(), threadLocalService.get());
		threadLocalService.closeEntry();
		log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, System.currentTimeMillis() - performanceMillis,
				docCount);
		performanceMillis = System.currentTimeMillis();
		if (docCount == 0) {
			throw new CustomException(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE, 204);
		}
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info(LoggingConstants.ANONYMIZED_RETRIEVE_PROCESS_END, (System.currentTimeMillis() - serviceStartTime));
	}

	@Override
	public void getDeanonimizedSessionLogs(SessionLogsRequestDto requestData,
													  String xPagopaHelpdUid,
													  String xPagopaCxType) throws IOException, LogExtractorException {
		log.info("Deanonimized session logs retrieve process - START - user={}, userType={}, ticketNumber={}, " +
				"jti={}, startDate={}, endDate={}", xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(),
				requestData.getJti(), requestData.getDateFrom(), requestData.getDateTo());
		long serviceStartTime = System.currentTimeMillis();
		long performanceMillis = 0;
//		File openSearchResponse = new FileUtilities().getFileWithRandomName(OS_RESULT, GenericConstants.TXT_EXTENSION);
//		FileOutputStream tmpOutStream = new FileOutputStream(openSearchResponse);
		int docCount = 0;

		log.info("Getting session activities' deanonimized history... ");
		performanceMillis = System.currentTimeMillis();
		S3DocumentDownloader s3 = new S3DocumentDownloader(awsProfile, s3Region, s3Bucket);
		openSearchApiHandler.setObserver(s3);
		openSearchApiHandler.addObserver(new DeanonimizationService(deanonimizationApiHandler, threadLocalService.get(), RecipientTypes.PF));
		threadLocalService.addEntry(OS_RESULT+GenericConstants.TXT_EXTENSION);
		docCount = openSearchApiHandler.getAnonymizedSessionLogsByJti(requestData.getJti(), requestData.getDateFrom(), requestData.getDateTo(), OutputStream.nullOutputStream());

		log.info("Query execution completed in {} ms, retrieved {} documents, deanonimizing results...",
				System.currentTimeMillis() - performanceMillis, docCount);
//		tmpOutStream.flush();
//		IOUtils.closeQuietly(tmpOutStream);
//		deanonimizationService.deanonimizeDocuments(openSearchResponse, RecipientTypes.PF, threadLocalService.get());
		threadLocalService.closeEntry();
		
		if (s3.getFileName()!=null) {
			threadLocalService.addEntry(s3.getFileName(),s3.getFileContent());
		}

//		Files.delete(openSearchResponse.toPath());
		log.info("Deanonimization completed in {} ms, constructing service response...", System.currentTimeMillis() - performanceMillis);
		performanceMillis = System.currentTimeMillis();
		if(docCount == 0) {
			throw new CustomException(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE, 204);
		}
		log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME, System.currentTimeMillis() - performanceMillis);
		log.info(LoggingConstants.DEANONIMIZED_RETRIEVE_PROCESS_END, (System.currentTimeMillis() - serviceStartTime));
	}
}
