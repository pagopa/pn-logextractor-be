package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.logextractor.dto.response.FileDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationHistoryResponseDto;
import it.gov.pagopa.logextractor.exception.CustomException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import it.gov.pagopa.logextractor.util.external.IStorageService;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiHandlerFactory;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationDownloadFileData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationLogService {
	@Autowired
	private ZipService zipService;
	
	@Autowired 
	private IStorageService s3ClientService;

	@Autowired
	private OpenSearchApiHandlerFactory openSearchApiHandlerFactory;
	
	@Autowired
	private NotificationApiHandler notificationApiHandler;
	
	public void getNotificationInfoLogs(String key, String zipPassword, NotificationInfoRequestDto requestData, String xPagopaHelpdUid,
			String xPagopaCxType) throws IOException {
		log.info("Notification data retrieve process - START - user={}, userType={}, ticketNumber={}, iun={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(), requestData.getIun());
		ZipInfo zipInfo = zipService.createZip(key, zipPassword, s3ClientService.uploadStreamV2(key));
		try {
			ArrayList<NotificationDownloadFileData> downloadableFiles = new ArrayList<>();
			long serviceStartTime = System.currentTimeMillis();
			double secondsToWait = 0;
			ObjectMapper mapper = new ObjectMapper();
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
			NotificationDownloadFileData aar = notificationApiHandler.getAARFileDownloadData(notificationHistory);
			log.info("AARs' keys retrieved in {} ms, getting notification documents' keys...",
					System.currentTimeMillis() - performanceMillis);
			if (aar!=null) {
				downloadFileData.add(aar);
			}
			
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
					log.warn("Cannot download notification {} for error: {}", currentDownloadData,ex.getMessage());
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
					String notifName = 
							currentDownloadableFile.getFileCategory() + "-" + currentDownloadableFile.getKey()+
							GenericConstants.PDF_EXTENSION;
					zipService.addEntry(zipInfo, notifName);
					if (notificationApiHandler.downloadToStream(currentDownloadableFile.getDownloadUrl(),
							zipInfo.getZos()) > 0) {
						log.error("Cannot download notification {}",notifName);
					}
					zipService.closeEntry(zipInfo);
				}
				log.info("Physical files retrieved in {} ms", System.currentTimeMillis() - performanceMillis);
	
				
				OutputStream out = zipInfo.getZos();
				zipService.addEntry(zipInfo, "dati.txt");
				int docsNumber = openSearchApiHandlerFactory.getOpenSearchApiHanlder().getAnonymizedLogsByIun(requestData.getIun(),
						notificationStartDate.toString(), notificationEndDate, out);
				zipService.closeEntry(zipInfo);
				log.info(LoggingConstants.QUERY_EXECUTION_COMPLETED_TIME, System.currentTimeMillis() - performanceMillis,
						docsNumber);
				performanceMillis = System.currentTimeMillis();
				log.info(LoggingConstants.SERVICE_RESPONSE_CONSTRUCTION_TIME,
						System.currentTimeMillis() - performanceMillis);
				log.info("Notification data retrieve process - END in {} ms",
						(System.currentTimeMillis() - serviceStartTime));
			}
			if (!filesNotDownloadable.isEmpty()) {
				zipService.addEntry(zipInfo, GenericConstants.ERROR_SUMMARY_FILE_NAME + ".txt");
				JsonUtilities jsonUtilities = new JsonUtilities();
				String failsToString = jsonUtilities.toString(jsonUtilities.toJson(filesNotDownloadable));
				OutputStreamWriter osw = new OutputStreamWriter(zipInfo.getZos());
				osw.write(failsToString);
				osw.flush();
				zipService.closeEntry(zipInfo);
			}
		}catch(Exception err) {
			log.error("Error processing NotificationLog Request", err);
			zipService.addEntryWithContent(zipInfo, "error.txt", err.getMessage());
		}
		zipService.close(zipInfo);
	}
	
	private Integer getRetryAfter(FileDownloadMetadataResponseDto downloadMetaData) {
		if (downloadMetaData == null || downloadMetaData.getDownload() == null) {
			return 0;
		}else {
			Integer ret = downloadMetaData.getDownload().getRetryAfter();
			return ret==null?0:ret;
		}
	}

}
