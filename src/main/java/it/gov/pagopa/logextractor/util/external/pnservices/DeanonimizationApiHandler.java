package it.gov.pagopa.logextractor.util.external.pnservices;

import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.config.ApplicationContextProvider;
import it.gov.pagopa.logextractor.dto.response.EnsureRecipientByExternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.util.RecipientTypes;

/**
 * A utility class containing methods that make calls to Piattaforma Notifiche
 * deanonimization external service
 */
public class DeanonimizationApiHandler {

	/**
	 * Method that makes a request to Piattaforma Notifiche external service to
	 * retrieve the unique identifier of a person, given the recipient type and tax
	 * id of a person
	 * 
	 * @param recipientType      represents the two values of the enum
	 *                           {@link RecipientType}.
	 * @param taxId              the tax id of a person
	 * @param externalServiceUrl the url of the external endpoint that the method
	 *                           needs to make a request to Piattaforma Notifiche
	 *                           service
	 * @return object of type {@link GetBasicDataResponseDto}, containing the unique
	 *         identifier of a person
	 * @throws HttpServerErrorException
	 */
	public GetBasicDataResponseDto getUniqueIdentifierForPerson(RecipientTypes recipientType, String taxId,
			String externalServiceUrl) throws HttpServerErrorException {
		RestTemplate client = (RestTemplate) ApplicationContextProvider.getBean("simpleRestTemplate");

		String URL = String.format(externalServiceUrl, recipientType.toString(), taxId);
		var response = client.getForObject(URL, EnsureRecipientByExternalIdResponseDto.class);

		return GetBasicDataResponseDto.builder().data(response.getInternalId()).build();
	}

	/**
	 * Method that makes a request to Piattaforma Notifiche external service to
	 * retrieve the tax code of a person, given the person's unique identifier
	 * 
	 * @param personId           the unique identifier of a person
	 * @param externalServiceUrl he url of the external endpoint that the method
	 *                           needs to make a request to Piattaforma Notifiche
	 *                           service
	 * @return object of type {@link GetBasicDataResponseDto}, containing the tax
	 *         code of a person
	 * @throws HttpServerErrorException
	 */
	public GetBasicDataResponseDto getTaxCodeForPerson(String personId, String externalServiceUrl)
			throws HttpServerErrorException {
		RestTemplate client = (RestTemplate) ApplicationContextProvider.getBean("simpleRestTemplate");

		String URL = String.format(externalServiceUrl, "123");
		var response = client.getForObject(URL, GetRecipientDenominationByInternalIdResponseDto.class);

		return GetBasicDataResponseDto.builder().data(response.getTaxId()).build();
	}
}
