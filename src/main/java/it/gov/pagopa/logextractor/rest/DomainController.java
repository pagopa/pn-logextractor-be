package it.gov.pagopa.logextractor.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import it.gov.pagopa.logextractor.dto.response.DomainResponseDto;

@RestController
@RequestMapping("/domains")
public class DomainController {
	
	@GetMapping("/extractionTypes")
	public ResponseEntity<List<DomainResponseDto>> getExtractionTypes(){
		return ResponseEntity.ok(null);
	}
}