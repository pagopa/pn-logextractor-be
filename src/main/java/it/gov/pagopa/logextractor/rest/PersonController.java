package it.gov.pagopa.logextractor.rest;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import it.gov.pagopa.logextractor.dto.request.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonTaxIdRequestDto;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.service.PersonService;
import it.gov.pagopa.logextractor.util.RecipientTypes;

@RestController
@RequestMapping("/logextractor/v1/persons")
public class PersonController {

	@Autowired
	PersonService personService;
	
	@PostMapping("/person-id")
	public ResponseEntity<GetBasicDataResponseDto> getPersonalPersonId(@Valid @RequestBody PersonPersonIdRequestDto personData) throws HttpServerErrorException, LogExtractorException {
		return ResponseEntity.ok(personService.getPersonId(RecipientTypes.valueOf(personData.getRecipientType()), personData.getTicketNumber(), personData.getTaxId().toUpperCase()));
	}
	
	@PostMapping("/tax-id")
	public ResponseEntity<GetBasicDataResponseDto> getPersonalTaxId(@Valid @RequestBody PersonTaxIdRequestDto personData) throws HttpServerErrorException, LogExtractorException {
		return ResponseEntity.ok(personService.getTaxId(personData.getPersonId()));
	}
}