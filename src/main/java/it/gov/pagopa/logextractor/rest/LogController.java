package it.gov.pagopa.logextractor.rest;

import it.gov.pagopa.logextractor.pn_logextractor_be.api.LogsApi;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import it.gov.pagopa.logextractor.service.LogService;

@RestController
public class LogController implements LogsApi {

	@Autowired
	LogService logService;

	@Override
	public ResponseEntity<BaseResponseDto> personActivityLogs(PersonLogsRequestDto personLogsRequestDto) throws Exception {
		if (Boolean.TRUE.equals(personLogsRequestDto.getDeanonimization())) {
			return ResponseEntity.ok().body(logService.getDeanonimizedPersonLogs(personLogsRequestDto));
		}
		return ResponseEntity.ok().body(logService.getAnonymizedPersonLogs(personLogsRequestDto));
	}

	@Override
	public ResponseEntity<BaseResponseDto> notificationInfoLogs(NotificationInfoRequestDto notificationInfoRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getNotificationInfoLogs(notificationInfoRequestDto));
	}

	@Override
	public ResponseEntity<BaseResponseDto> notificationsInMonth(MonthlyNotificationsRequestDto monthlyNotificationsRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getMonthlyNotifications(monthlyNotificationsRequestDto));
	}

	@Override
	public ResponseEntity<BaseResponseDto> processLogs(TraceIdLogsRequestDto traceIdLogsRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getTraceIdLogs(traceIdLogsRequestDto));
	}
	
	@Override
	public ResponseEntity<BaseResponseDto> sessionLogs(SessionLogsRequestDto sessionLogsRequestDto) throws Exception {
		if (Boolean.TRUE.equals(sessionLogsRequestDto.getDeanonimization())) {
			return ResponseEntity.ok(logService.getDeanonimizedSessionLogs(sessionLogsRequestDto));
		}
		return ResponseEntity.ok(logService.getAnonymizedSessionLogs(sessionLogsRequestDto));
	}
}