package it.gov.pagopa.logextractor.rest;

import it.gov.pagopa.logextractor.pn_logextractor_be.api.LogsApi;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
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

	//TODO: Ivan: a valle restituire byte[]
	@Override
	public ResponseEntity<BaseResponseDto> notificationInfoLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, NotificationInfoRequestDto notificationInfoRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getNotificationInfoLogs(notificationInfoRequestDto,
				xPagopaUid, xPagopaCxType));
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