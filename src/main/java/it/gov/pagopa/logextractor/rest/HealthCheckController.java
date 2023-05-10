package it.gov.pagopa.logextractor.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.pn_logextractor_be.api.StatusApi;

@RestController
public class HealthCheckController implements StatusApi {

	@Override
	public ResponseEntity<Void> healthStatus() {
		return ResponseEntity.ok().build();
	}
}
