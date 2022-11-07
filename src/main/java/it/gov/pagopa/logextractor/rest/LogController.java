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
	public ResponseEntity<BaseResponseDto> getPersonActivityLogs(PersonLogsRequestDto personLogsRequestDto) throws Exception {
		if (Boolean.TRUE.equals(personLogsRequestDto.getDeanonimization())) {
			return ResponseEntity.ok().body(logService.getDeanonimizedPersonLogs(personLogsRequestDto.getRecipientType(),
					personLogsRequestDto.getDateFrom(), personLogsRequestDto.getDateTo(),
					personLogsRequestDto.getTicketNumber(), personLogsRequestDto.getTaxId(),
					personLogsRequestDto.getIun()));
		}
		return ResponseEntity.ok().body(logService.getAnonymizedPersonLogs(personLogsRequestDto.getDateFrom(),
				personLogsRequestDto.getDateTo(), personLogsRequestDto.getTicketNumber(),
				personLogsRequestDto.getIun(), personLogsRequestDto.getPersonId()));
	}

	@Override
	public ResponseEntity<BaseResponseDto> getNotificationInfoLogs(NotificationInfoRequestDto notificationInfoRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getNotificationInfoLogs(notificationInfoRequestDto.getTicketNumber(),
				notificationInfoRequestDto.getIun()));
	}

	@Override
	public ResponseEntity<BaseResponseDto> getNotificationsInMonth(MonthlyNotificationsRequestDto monthlyNotificationsRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getMonthlyNotifications(monthlyNotificationsRequestDto.getTicketNumber(),
				monthlyNotificationsRequestDto.getReferenceMonth(), monthlyNotificationsRequestDto.getEndMonth(),
				monthlyNotificationsRequestDto.getPublicAuthorityName()));
	}

	@Override
	public ResponseEntity<BaseResponseDto> getProcessLogs(TraceIdLogsRequestDto traceIdLogsRequestDto) throws Exception {
		return ResponseEntity.ok().body(logService.getTraceIdLogs(traceIdLogsRequestDto.getDateFrom(),
				traceIdLogsRequestDto.getDateTo(), traceIdLogsRequestDto.getTraceId()));
	}
}