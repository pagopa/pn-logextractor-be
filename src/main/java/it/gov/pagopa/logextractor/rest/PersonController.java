package it.gov.pagopa.logextractor.rest;

import javax.validation.Valid;
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

import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.dto.request.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonTaxIdRequestDto;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.service.PersonService;
import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.RecipientTypes;

@RestController
@RequestMapping("/logextractor/v1/persons")
public class PersonController {

	@Autowired
	PersonService personService;
	
	@PostMapping("/person-id")
	public ResponseEntity<GetBasicDataResponseDto> getPersonalPersonId(@Valid @RequestBody PersonPersonIdRequestDto personData) {
		return ResponseEntity.ok(personService.getPersonId(RecipientTypes.valueOf(personData.getRecipientType()), personData.getTicketNumber(), personData.getTaxId()));
	}
	
	@PostMapping("/tax-id")
	public ResponseEntity<GetBasicDataResponseDto> getPersonalTaxId(@Valid @RequestBody PersonTaxIdRequestDto personData) {
		return ResponseEntity.ok(personService.getTaxId(personData.getPersonId()));
	}
}