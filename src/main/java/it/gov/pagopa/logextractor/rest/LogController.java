package it.gov.pagopa.logextractor.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import it.gov.pagopa.logextractor.dto.response.DownloadLogResponseDto;

@RestController
@RequestMapping("/logextractor/v1/logs")
public class LogController {

	@GetMapping(value = "/persons", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getPersonActivityLogs(@RequestParam(required = true) String extractionType, 
																		@RequestParam(required = true) int ticketNumber,
																		@RequestParam(required = false) Integer iun, 
																		@RequestParam(required = false) Integer months, 
																		@RequestParam(required = true) boolean deanonimization,
																		@RequestParam(required = false) String taxId, 
																		@RequestParam(required = false) String personId){
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(value = "/operators", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getOperatorsActivityLogs(@RequestParam(required = true) String extractionType, 
																		   @RequestParam(required = true) int ticketNumber, 
																		   @RequestParam(required = true) int months, 
																		   @RequestParam(required = false) String taxId) {
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(value = "/notifications", produces="application/zip")
	public ResponseEntity<DownloadLogResponseDto> getNotificationLogs(@RequestParam(required = true) String extractionType, 
																	  @RequestParam(required = true) int ticketNumber,
																	  @RequestParam(required = false) Integer iun,
																	  @RequestParam(required = false) Integer referenceMonth,
																	  @RequestParam(required = false) String ipaCode){
		return ResponseEntity.ok(null);
	}
}