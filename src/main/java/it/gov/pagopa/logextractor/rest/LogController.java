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

	@Override
	public ResponseEntity<BaseResponseDto> personActivityLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, PersonLogsRequestDto personLogsRequestDto) throws Exception {
		if (Boolean.TRUE.equals(personLogsRequestDto.getDeanonimization())) {
			return ResponseEntity.ok().body(logService.getDeanonimizedPersonLogs(personLogsRequestDto,
					xPagopaUid, xPagopaCxType));
		}
		return ResponseEntity.ok().body(logService.getAnonymizedPersonLogs(personLogsRequestDto,
				xPagopaUid, xPagopaCxType));
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
		if (resp instanceof DownloadArchiveResponseDto) {
			DownloadArchiveResponseDto dard = (DownloadArchiveResponseDto) resp;
			HttpHeaders headers = new HttpHeaders(); 
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=notificationInfoLogs.zip");
			headers.add("password",dard.getPassword());
			Resource resource = new ByteArrayResource(dard.getZip());
			return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
		}else {
			throw new CustomException(resp);
		}
	}

	@Override
	public ResponseEntity<BaseResponseDto> notificationsInMonth(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, MonthlyNotificationsRequestDto monthlyNotificationsRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getMonthlyNotifications(monthlyNotificationsRequestDto,
				xPagopaUid, xPagopaCxType));
	}

	@Override
	public ResponseEntity<BaseResponseDto> processLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, TraceIdLogsRequestDto traceIdLogsRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getTraceIdLogs(traceIdLogsRequestDto,
				xPagopaUid, xPagopaCxType));
	}
	
	@Override
	public ResponseEntity<BaseResponseDto> sessionLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, SessionLogsRequestDto sessionLogsRequestDto) throws Exception {
		if (Boolean.TRUE.equals(sessionLogsRequestDto.getDeanonimization())) {
			return ResponseEntity.ok(logService.getDeanonimizedSessionLogs(sessionLogsRequestDto,
					xPagopaUid, xPagopaCxType));
		}
		return ResponseEntity.ok(logService.getAnonymizedSessionLogs(sessionLogsRequestDto,
				xPagopaUid, xPagopaCxType));
	}
}