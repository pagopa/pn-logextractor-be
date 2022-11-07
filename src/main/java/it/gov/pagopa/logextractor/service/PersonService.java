package it.gov.pagopa.logextractor.service;

import it.gov.pagopa.logextractor.pn_logextractor_be.model.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;
import org.springframework.web.client.HttpServerErrorException;
import it.gov.pagopa.logextractor.exception.LogExtractorException;

/**
 * An interface containing all methods for persons
 */
public interface PersonService {
	
	/**
	 * Method that retrieves a person's tax code corresponding to the input internal code
	 * @param personId The person's internal code to obtain the tax code from
	 * @return object of type {@link GetBasicDataResponseDto}, containing the tax code of a person
	 * @throws HttpServerErrorException in case of an error during the integration process with external services
	 * */
	GetBasicDataResponseDto getTaxId(String personId) throws HttpServerErrorException, LogExtractorException;
	
	/**
	 * Method that retrieves a person's internal code corresponding to the input tax code
	 * @param recipientType The person's type, can be one of the {@link RecipientTypes} object values
	 * @param ticketNumber The ticket number associated to the operation
	 * @return object of type {@link GetBasicDataResponseDto}, containing the internal code of a person
	 * @throws HttpServerErrorException in case of an error during the integration process with external services
	 * */
	GetBasicDataResponseDto getPersonId(RecipientTypes recipientType, String ticketNumber, String taxId) throws HttpServerErrorException, LogExtractorException;
}
