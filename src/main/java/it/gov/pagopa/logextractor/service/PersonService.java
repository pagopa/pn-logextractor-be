package it.gov.pagopa.logextractor.service;

import org.springframework.web.client.HttpServerErrorException;

import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.util.RecipientTypes;

/**
 * An interface containing all methods for persons
 */
public interface PersonService {

	/**
	 * Service method that makes a request to Piattaforma Notifiche external service
	 * and retrieves basic data about a person. If ticketNumber and taxId are
	 * present, the method retrieves a unique identifier, which is the encrypted
	 * fiscal code of a person. If personId is present, the method retrieves the tax
	 * code of a person.
	 * 
	 * @param extractionType required parameter, which represents the integer value
	 *                       of the extraction type
	 * @param recipientType  required parameter, which represents the two values of
	 *                       the enum {@link RecipientType}
	 * @param ticketNumber   the ticket number of a person
	 * @param taxId          the tax id of a person
	 * @param personId       the unique identifier of a person
	 * @return basic data for a person, depending on which parameters are present
	 * @throws HttpServerErrorException
	 */
	GetBasicDataResponseDto getPersonsBasicData(Integer extractionType, RecipientTypes recipientType, String ticketNumber,
			String taxId, String personId) throws HttpServerErrorException;
}
