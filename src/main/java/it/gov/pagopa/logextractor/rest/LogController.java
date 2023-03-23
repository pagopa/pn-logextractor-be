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
	public ResponseEntity<BaseResponseDto> personActivityLogs(String xPagopaPnUid, String xPagopaPnCxType, PersonLogsRequestDto personLogsRequestDto) throws Exception {
		if (Boolean.TRUE.equals(personLogsRequestDto.getDeanonimization())) {
			return ResponseEntity.ok().body(logService.getDeanonimizedPersonLogs(personLogsRequestDto,
					xPagopaPnUid, xPagopaPnCxType));
		}
		return ResponseEntity.ok().body(logService.getAnonymizedPersonLogs(personLogsRequestDto,
				xPagopaPnUid, xPagopaPnCxType));
	}

	@Override
	public ResponseEntity<BaseResponseDto> notificationInfoLogs(String xPagopaPnUid, String xPagopaPnCxType, NotificationInfoRequestDto notificationInfoRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getNotificationInfoLogs(notificationInfoRequestDto,
				xPagopaPnUid, xPagopaPnCxType));
	}

	@Override
	public ResponseEntity<BaseResponseDto> notificationsInMonth(String xPagopaPnUid, String xPagopaPnCxType, MonthlyNotificationsRequestDto monthlyNotificationsRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getMonthlyNotifications(monthlyNotificationsRequestDto,
				xPagopaPnUid, xPagopaPnCxType));
	}

	@Override
	public ResponseEntity<BaseResponseDto> processLogs(String xPagopaPnUid, String xPagopaPnCxType, TraceIdLogsRequestDto traceIdLogsRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getTraceIdLogs(traceIdLogsRequestDto,
				xPagopaPnUid, xPagopaPnCxType));
	}

	@Override
	public ResponseEntity<BaseResponseDto> sessionLogs(String xPagopaPnUid, String xPagopaPnCxType, SessionLogsRequestDto sessionLogsRequestDto) throws Exception {
		if (Boolean.TRUE.equals(sessionLogsRequestDto.getDeanonimization())) {
			return ResponseEntity.ok(logService.getDeanonimizedSessionLogs(sessionLogsRequestDto,
					xPagopaPnUid, xPagopaPnCxType));
		}
		return ResponseEntity.ok(logService.getAnonymizedSessionLogs(sessionLogsRequestDto, xPagopaPnUid, xPagopaPnCxType));
	}
}