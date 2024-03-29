package it.gov.pagopa.logextractor.rest;

import it.gov.pagopa.logextractor.pn_logextractor_be.api.PersonsApi;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonTaxIdRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import it.gov.pagopa.logextractor.service.PersonService;

@RestController
public class PersonController implements PersonsApi {

	@Autowired
	PersonService personService;

	@Override
	public ResponseEntity<GetBasicDataResponseDto> personalPersonId(String xPagopaPnUid, String xPagopaPnCxType, PersonPersonIdRequestDto personPersonIdRequestDto) throws Exception {
		return ResponseEntity.ok(personService.getPersonId(personPersonIdRequestDto, xPagopaPnUid, xPagopaPnCxType));
	}

	@Override
	public ResponseEntity<GetBasicDataResponseDto> personalTaxId(String xPagopaPnUid, String xPagopaPnCxType, PersonTaxIdRequestDto personTaxIdRequestDto) throws Exception {
		return ResponseEntity.ok(personService.getTaxId(personTaxIdRequestDto, xPagopaPnUid, xPagopaPnCxType));
	}
}