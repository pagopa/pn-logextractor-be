package it.gov.pagopa.logextractor.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logextractor/v1/health-check")
public class HealthCheckController {

	@GetMapping("/status")
	public ResponseEntity getHealthStatus() {
		return ResponseEntity.ok().build();
	}
}
