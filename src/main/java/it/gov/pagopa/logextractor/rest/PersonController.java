package it.gov.pagopa.logextractor.rest;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.service.PersonService;
import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.RecipientTypes;

@RestController
@RequestMapping("/logextractor/v1/persons")
public class PersonController {

	@Autowired
	PersonService personService;

	/**
	 * A controller method used to retrieve the basic data of a person (the basic
	 * data would be either unique identifier or tax code)
	 * 
	 * @param recipientType  required parameter, which represents the two values of
	 *                       the enum {@link RecipientType}
	 * @param ticketNumber   the ticket number of a person, this parameter should be
	 *                       passed if the unique identifier should be retrieved as
	 *                       basic data
	 * @param taxId          the tax id of a person, this parameter should be passed
	 *                       if the unique identifier should be retrieved as basic
	 *                       data
	 * @param personId       the unique identifier of a person, this parameter
	 *                       should be passed if the tax code should be retrieved as
	 *                       basic data
	 * @return basic data of a person, depending on which parameters are present
	 */
	@GetMapping("/basicData")
	public ResponseEntity<GetBasicDataResponseDto> getBasicData(@RequestParam(required = true) RecipientTypes recipientType,
			@RequestParam(required = false) @Pattern(regexp = Constants.ALPHA_NUMERIC_WITHOUT_SPECIAL_CHAR_PATTERN, message = "Invalid ticket number") String ticketNumber,
			@RequestParam(required = false) @Size(min = 16, max = 16) @Pattern(regexp = Constants.FISCAL_CODE_PATTERN, message = "Invalid Tax ID") String taxId,
			@RequestParam(required = false) @Size(min = 1, max = 100, message = "Invalid person id") @Pattern(regexp = Constants.INTERNAL_ID_PATTERN) String personId) {
		return ResponseEntity
				.ok(personService.getPersonsBasicData(recipientType, ticketNumber, taxId, personId));
	}
}