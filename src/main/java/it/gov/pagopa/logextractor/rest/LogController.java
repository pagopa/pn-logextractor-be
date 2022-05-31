package it.gov.pagopa.logextractor.rest;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import it.gov.pagopa.logextractor.dto.response.DownloadLogResponseDto;
import it.gov.pagopa.logextractor.service.LogService;
import it.gov.pagopa.logextractor.util.Constants;

@RestController
@RequestMapping("/logextractor/v1/logs")
public class LogController {

	@Autowired
	LogService logService;

	@GetMapping(value = "/persons", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getPersonActivityLogs(@RequestParam(required = true) @Pattern(regexp = Constants.ALPHA_NUMERIC_WITHOUT_SPECIAL_CHAR_PATTERN, message = "Invalid ticket number") String ticketNumber,
												@RequestParam(required = false) @Pattern(regexp = Constants.INPUT_DATE_FORMAT) String referenceDate,
												@RequestParam(required = false) @Pattern(regexp = Constants.INPUT_DATE_FORMAT) String dateFrom,
												@RequestParam(required = false) @Pattern(regexp = Constants.INPUT_DATE_FORMAT) String dateTo,
												@RequestParam(required = false) Integer iun,
												@RequestParam(required = true) boolean deanonimization,
												@RequestParam(required = false) @Size(min = 16, max = 16) @Pattern(regexp = Constants.FISCAL_CODE_PATTERN, message = "Invalid Tax ID") String taxId, 
												@RequestParam(required = false) @Size(min = 1, max = 100, message = "Invalid person id") @Pattern(regexp = Constants.INTERNAL_ID_PATTERN) String personId){
		// use case 4 & 5
		if (deanonimization) {
			
		}
		
		// use case 7 & 8
		return ResponseEntity.ok(logService.getPersonLogs(dateFrom, dateTo, referenceDate, ticketNumber, iun, personId));
	}
	
	
	@GetMapping(value = "/operators", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getOperatorsActivityLogs(@RequestParam(required = true) int ticketNumber, 
											   @RequestParam(required = false) @Pattern(regexp = Constants.INPUT_DATE_FORMAT) String dateFrom ,
											   @RequestParam(required = false) @Pattern(regexp = Constants.INPUT_DATE_FORMAT) String dateTo,
											   @RequestParam(required = false) String taxId) {
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(value = "/notifications/info", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getNotificationInfoLogs(@RequestParam(required = true) int ticketNumber,
					  							@RequestParam(required = true) Integer iun){
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(value = "/notifications/monthly", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getNotificationMonthlyLogs(@RequestParam(required = true) int ticketNumber,
											  @RequestParam(required = true) @Pattern(regexp = Constants.INPUT_DATE_FORMAT) String referenceMonth,
											  @RequestParam(required = true) String ipaCode){
		return ResponseEntity.ok(null);
	}
}