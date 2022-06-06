package it.gov.pagopa.logextractor.rest;

import java.io.IOException;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.dto.request.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.dto.response.DownloadLogResponseDto;
import it.gov.pagopa.logextractor.service.LogService;
import it.gov.pagopa.logextractor.util.Constants;

@RestController
@RequestMapping("/logextractor/v1/logs")
public class LogController {

	@Autowired
	LogService logService;

	@PostMapping(value = "/persons", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getPersonActivityLogs(@RequestBody PersonLogsRequestDto personLogsDetails) throws IOException {
		// use case 3 & 4
		if (personLogsDetails.isDeanonimization()) {
			
		}
		
		// use case 7 & 8
		return ResponseEntity
				.ok(logService.getPersonLogs(personLogsDetails.getDateFrom(), personLogsDetails.getDateTo(),
						personLogsDetails.getReferenceDate(), personLogsDetails.getTicketNumber(),
						personLogsDetails.getIun(), personLogsDetails.getPersonId(), personLogsDetails.getPassword()));	}
	
	
	@GetMapping(value = "/operators", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getOperatorsActivityLogs(@RequestParam(required = true) int ticketNumber, 
											   @RequestParam(required = false) @Pattern(regexp = Constants.INPUT_DATE_FORMAT) String dateFrom ,
											   @RequestParam(required = false) @Pattern(regexp = Constants.INPUT_DATE_FORMAT) String dateTo,
											   @RequestParam(required = false) String taxId) {
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(value = "/notifications/info", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getNotificationInfoLogs(@RequestParam(required = true) String ticketNumber,
					  							@RequestParam(required = true) Integer iun){
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(value = "/notifications/monthly", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getNotificationMonthlyLogs(@RequestParam(required = true) String ticketNumber,
											  @RequestParam(required = true) @Pattern(regexp = Constants.INPUT_MONTH_FORMAT) String referenceMonth,
											  @RequestParam(required = true) String ipaCode) {
		
//		return ResponseEntity.ok(logService.getMonthlyNotifications(ticketNumber, referenceMonth, ipaCode));
		return null;
	}
}