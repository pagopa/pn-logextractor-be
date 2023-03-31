package it.gov.pagopa.logextractor.rest;

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.exception.CustomException;
import it.gov.pagopa.logextractor.pn_logextractor_be.api.LogsApi;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.SessionLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.service.LogService;
import it.gov.pagopa.logextractor.service.ThreadLocalOutputStreamService;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@CrossOrigin(allowedHeaders = "password,content-disposition",exposedHeaders = "password,content-disposition")
public class LogController implements LogsApi {

	@Autowired
	LogService logService;
	
	@Autowired
	ThreadLocalOutputStreamService threadLocalService;

	private  ResponseEntity<Resource> handleResponse(BaseResponseDto resp, String attachmentName) {
		File zipFile = null;
		try {
			if (resp instanceof DownloadArchiveResponseDto) {
				DownloadArchiveResponseDto dard = (DownloadArchiveResponseDto) resp;
				HttpHeaders headers = new HttpHeaders(); 
				headers.add("Access-Control-Expose-Headers", "password,content-disposition");
				headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s.zip", attachmentName));
				headers.add("password", dard.getPassword());
				headers.add("Content-Type",MediaType.APPLICATION_OCTET_STREAM_VALUE);
				zipFile = dard.getZipFile();
				Resource resource = new InputStreamResource(new FileInputStream(zipFile));
				return ResponseEntity.ok().headers(headers).body(resource);
			}else {
				throw new CustomException(resp);
			}
		} catch (Exception e) {
			log.error("Error getting zip file", e);
			throw new CustomException(new BaseResponseDto());
		}
		finally {
			if (zipFile != null) {
				FileUtils.deleteQuietly(zipFile);
			}
		}
	}
	
	@Autowired
	private HttpServletResponse httpServletResponse;
	
	@Override
	public ResponseEntity<Resource> personActivityLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, PersonLogsRequestDto personLogsRequestDto
   		 ) throws Exception {
		
//		this.threadLocalService.set(httpServletResponse.getOutputStream());
		BaseResponseDto resp;
		if (Boolean.TRUE.equals(personLogsRequestDto.getDeanonimization())) {
			resp = logService.getDeanonimizedPersonLogs(personLogsRequestDto, xPagopaUid, xPagopaCxType);
		}else {
			resp = logService.getAnonymizedPersonLogs(personLogsRequestDto, xPagopaUid, xPagopaCxType); 
		}
		return handleResponse(resp, "personActivityLogs");
	}

	//TODO: Capire con Marco il desiderata e lo status code da opeani 400/500
	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<BaseResponseDto> handleCustomException(CustomException e){
		return ResponseEntity.status(200).body(e.getDto());
	}
	
	@Override
	public ResponseEntity<Resource> notificationInfoLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, NotificationInfoRequestDto notificationInfoRequestDto) throws Exception {
		
		BaseResponseDto resp = logService.getNotificationInfoLogs(notificationInfoRequestDto,xPagopaUid, xPagopaCxType);
		return handleResponse(resp, "notificationInfoLogs");
	}

	@Override
	public ResponseEntity<Resource> notificationsInMonth(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, MonthlyNotificationsRequestDto monthlyNotificationsRequestDto) throws Exception {
		BaseResponseDto resp = logService.getMonthlyNotifications(monthlyNotificationsRequestDto, xPagopaUid, xPagopaCxType);
		return handleResponse(resp, "notificationsInMonth");
	}

	@Override
	public ResponseEntity<Resource> processLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, TraceIdLogsRequestDto traceIdLogsRequestDto) throws Exception {
		BaseResponseDto resp = logService.getTraceIdLogs(traceIdLogsRequestDto, xPagopaUid, xPagopaCxType);
		return handleResponse(resp, "processLogs");
	}
	
	@Override
	public ResponseEntity<Resource> sessionLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, SessionLogsRequestDto sessionLogsRequestDto) throws Exception {
		BaseResponseDto resp;
		if (Boolean.TRUE.equals(sessionLogsRequestDto.getDeanonimization())) {
			resp = logService.getDeanonimizedSessionLogs(sessionLogsRequestDto, xPagopaUid, xPagopaCxType);
		}else {
			resp = logService.getAnonymizedSessionLogs(sessionLogsRequestDto, xPagopaUid, xPagopaCxType);
		}
		return handleResponse(resp, "sessionLogs");
	}
}