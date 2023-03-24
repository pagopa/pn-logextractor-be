package it.gov.pagopa.logextractor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.exception.CustomException;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.api.LogsApi;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.SessionLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.service.LogService;

@RestController
public class LogController implements LogsApi {

	@Autowired
	LogService logService;

	private  ResponseEntity<Resource> handleResponse(BaseResponseDto resp, String attachmentName) {
		if (resp instanceof DownloadArchiveResponseDto) {
			DownloadArchiveResponseDto dard = (DownloadArchiveResponseDto) resp;
			HttpHeaders headers = new HttpHeaders(); 
			headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s.zip", attachmentName));
			headers.add("password", dard.getPassword());
			Resource resource = new ByteArrayResource(dard.getZip());
			return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
		}else {
			throw new CustomException(resp);
		}
	}
	
	@Override
	public ResponseEntity<Resource> personActivityLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, PersonLogsRequestDto personLogsRequestDto) throws Exception {
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
		return ResponseEntity.status(400).body(e.getDto());
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