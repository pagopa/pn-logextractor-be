package it.gov.pagopa.logextractor.service;

import it.gov.pagopa.logextractor.pn_logextractor_be.model.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonTaxIdRequestDto;
import org.springframework.web.client.HttpServerErrorException;
import it.gov.pagopa.logextractor.exception.LogExtractorException;

/**
 * An interface containing all methods for persons
 */
public interface PersonService {
	
	/**
	 * Method that retrieves a person's tax code corresponding to the input internal code
	 * @param requestData The input data of type {@link PersonTaxIdRequestDto}
	 * @return object of type {@link GetBasicDataResponseDto}, containing the tax code of a person
	 * @throws HttpServerErrorException in case of an error during the integration process with external services
	 * @throws LogExtractorException if the external service response is "null", null, blank or has 0 length
	 * */
	GetBasicDataResponseDto getTaxId(PersonTaxIdRequestDto requestData,
									 String xPagopaHelpdUid,
									 String xPagopaCxType) throws HttpServerErrorException, LogExtractorException;
	
	/**
	 * Method that retrieves a person's internal code corresponding to the input tax code
	 * @param requestData The input data of type {@link PersonPersonIdRequestDto}
	 * @param xPagopaHelpdUid 
	 * @return object of type {@link GetBasicDataResponseDto}, containing the internal code of a person
	 * @throws HttpServerErrorException in case of an error during the integration process with external services
	 * @throws LogExtractorException if the external service response is "null", null or blank
	 * */
	GetBasicDataResponseDto getPersonId(PersonPersonIdRequestDto requestData,
										String xPagopaHelpdUid,
										String xPagopaCxType) throws HttpServerErrorException, LogExtractorException;
}
