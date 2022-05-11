package it.gov.pagopa.logextractor.rest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import it.gov.pagopa.logextractor.dto.response.DownloadLogResponseDto;

@RestController
@RequestMapping("/logs")
public class LogController {

	@GetMapping(name = "/persons", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<DownloadLogResponseDto> getPersonActivityLogs(@RequestParam(required = true) String extractionType, 
																		@RequestParam(required = true) int ticketNumber,
																		@RequestParam(required = false) Integer iun, 
																		@RequestParam(required = false) Integer months, 
																		@RequestParam(required = true) boolean deanonimization,
																		@RequestHeader(name = "fiscal-code", required = false) String taxId, 
																		@RequestHeader(name = "person-id", required = false) String personId){
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(name = "/operators", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<DownloadLogResponseDto> getOperatorsActivityLogs(@RequestParam(required = true) String extractionType, 
																		   @RequestParam(required = true) int ticketNumber, 
																		   @RequestParam(required = true) int months, 
																		   @RequestHeader(name = "fiscal-code", required = false) String taxId) {
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(name = "/notifications", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<DownloadLogResponseDto> getNotificationLogs(@RequestParam(required = true) String extractionType, 
																	  @RequestParam(required = true) int ticketNumber,
																	  @RequestParam(required = false) Integer iun,
																	  @RequestParam(required = false) Integer referenceMonth,
																	  @RequestHeader(name = "person-id", required = false) String personId){
		return ResponseEntity.ok(null);
	}
}