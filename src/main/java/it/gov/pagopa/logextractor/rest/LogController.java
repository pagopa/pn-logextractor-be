package it.gov.pagopa.logextractor.rest;

import java.io.IOException;
import java.text.ParseException;

import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.dto.request.PersonLogsRequestDto;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.response.PasswordResponseDto;
import it.gov.pagopa.logextractor.service.LogService;
import it.gov.pagopa.logextractor.util.Constants;

@RestController
@RequestMapping("/logextractor/v1/logs")
public class LogController {

	@Autowired
	LogService logService;

	@PostMapping(value = "/persons", produces="application/zip")
	public ResponseEntity<PasswordResponseDto> getPersonActivityLogs(@RequestBody PersonLogsRequestDto personLogsDetails) throws IOException {
		if (personLogsDetails.isDeanonimization()) {
			
		}
		
		// use case 7 & 8
		return ResponseEntity
				.ok(logService.getPersonLogs(personLogsDetails.getDateFrom(), personLogsDetails.getDateTo(),
						personLogsDetails.getReferenceDate(), personLogsDetails.getTicketNumber(),
						personLogsDetails.getIun(), personLogsDetails.getPersonId(), personLogsDetails.getPassword()));	}
	
	
	@GetMapping(value = "/operators", produces="application/zip")
	public ResponseEntity<PasswordResponseDto> getOperatorsActivityLogs(@RequestParam(required = true) int ticketNumber, 
											   @RequestParam(required = false) @Pattern(regexp = Constants.INPUT_DATE_FORMAT) String dateFrom ,
											   @RequestParam(required = false) @Pattern(regexp = Constants.INPUT_DATE_FORMAT) String dateTo,
											   @RequestParam(required = false) String taxId) {
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(value = "/notifications/info", produces="application/zip")
	public ResponseEntity<PasswordResponseDto> getNotificationInfoLogs(@RequestParam(required = true) String ticketNumber,
					  							@RequestParam(required = true) Integer iun){
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(value = "/notifications/monthly", produces="application/zip")
	public ResponseEntity<PasswordResponseDto> getNotificationMonthlyLogs(@RequestParam(required = true) String ticketNumber,
											  @RequestParam(required = true) @Pattern(regexp = Constants.INPUT_MONTH_FORMAT) String referenceMonth,
											  @RequestParam(required = true) String ipaCode) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, ParseException{
		
//		return ResponseEntity.ok(logService.getMonthlyNotifications(ticketNumber, referenceMonth, ipaCode));
		return null;
	}
	
	@GetMapping(value = "/logs/passwords", produces = "application/json")
	public ResponseEntity<PasswordResponseDto> getPassword(){
		return ResponseEntity.ok(logService.createPassword());
	}
}