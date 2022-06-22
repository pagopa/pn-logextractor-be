package it.gov.pagopa.logextractor.rest;

import java.io.IOException;
import java.text.ParseException;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.dto.request.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.dto.request.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.dto.request.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.service.LogService;
import it.gov.pagopa.logextractor.service.PersonService;

@RestController
@RequestMapping("/logextractor/v1/logs")
@EnableCaching
public class LogController {

	@Autowired
	LogService logService;
	@Autowired
	PersonService personService;
    
	@PostMapping(value = "/persons", produces="application/json")
	public ResponseEntity<DownloadArchiveResponseDto> getPersonActivityLogs(@Valid @RequestBody PersonLogsRequestDto personLogsDetails) throws IOException {
		if (personLogsDetails.isDeanonimization()) {
			return ResponseEntity.ok().body(logService.getDeanonymizedPersonLogs(personLogsDetails.getRecipientType(), personLogsDetails.getDateFrom(), personLogsDetails.getDateTo(), 
					personLogsDetails.getTicketNumber(), personLogsDetails.getTaxId(),personLogsDetails.getIun()));
		}
		return ResponseEntity.ok().body(logService.getAnonymizedPersonLogs(personLogsDetails.getDateFrom(), personLogsDetails.getDateTo(), 
										personLogsDetails.getTicketNumber(), personLogsDetails.getIun(), personLogsDetails.getPersonId()));
	}
	
	@PostMapping(value = "/notifications/info", produces="application/json")
	public void getNotificationInfoLogs(@RequestBody NotificationInfoRequestDto notificationInfo){
		
	}
	
	@PostMapping(value = "/notifications/monthly", produces="application/json")
	public ResponseEntity<DownloadArchiveResponseDto> getNotificationMonthlyLogs(@RequestBody MonthlyNotificationsRequestDto monthlyNotificationsData) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, ParseException{
		return ResponseEntity.ok().body(logService.getMonthlyNotifications(monthlyNotificationsData.getTicketNumber(),
																	monthlyNotificationsData.getReferenceMonth(),
																	monthlyNotificationsData.getIpaCode()));
	}

	@PostMapping(value = "/processes", produces = "application/json")
	public ResponseEntity<DownloadArchiveResponseDto> getNotificationTraceIdLogs(@RequestBody TraceIdLogsRequestDto traceIdLogsDetails) throws IOException {
		return ResponseEntity.ok().body(logService.getTraceIdLogs(traceIdLogsDetails.getDateFrom(),
				traceIdLogsDetails.getDateTo(), traceIdLogsDetails.getTraceId()));
	}
	
}