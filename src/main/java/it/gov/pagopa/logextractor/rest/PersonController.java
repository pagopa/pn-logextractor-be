package it.gov.pagopa.logextractor.rest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;

@RestController
@RequestMapping("/persons")
public class PersonController {
	
	@GetMapping(name = "/basicData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<GetBasicDataResponseDto> getBasicData(@RequestParam(required = true) String extractionType,
																@RequestParam(required = false) int ticketNumber,
																@RequestHeader(name = "fiscal-code", required = false) String taxId,
																@RequestHeader(name = "person-id", required = false) String personId){
		return ResponseEntity.ok(null);
	}
}