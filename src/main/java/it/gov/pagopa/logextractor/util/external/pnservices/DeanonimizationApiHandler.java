package it.gov.pagopa.logextractor.util.external.pnservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.dto.request.EnsureRecipientByExternalIdRequestDto;
import it.gov.pagopa.logextractor.dto.response.EnsureRecipientByExternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.util.RecipientTypes;

/**
 * A utility class containing methods that make calls to Piattaforma Notifiche
 * deanonimization external service
 */

@Component
@EnableCaching
public class DeanonimizationApiHandler {

	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;

	
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
	
	@Cacheable(cacheNames="services")
	public GetBasicDataResponseDto getUniqueIdentifierForPerson(RecipientTypes recipientType, String taxId, String externalServiceUrl) {
		String url = String.format(externalServiceUrl, recipientType.toString());
		EnsureRecipientByExternalIdRequestDto requestBody = EnsureRecipientByExternalIdRequestDto.builder().taxId(taxId).build();
		HttpEntity<String> request =  new HttpEntity<String>(requestBody.toString());
		EnsureRecipientByExternalIdResponseDto response = client.postForObject(url, request, EnsureRecipientByExternalIdResponseDto.class);
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
	@Cacheable(cacheNames="services")
	public GetBasicDataResponseDto getTaxCodeForPerson(String personId, String externalServiceUrl) {
		String url = String.format(externalServiceUrl, personId);
		GetRecipientDenominationByInternalIdResponseDto response = client.getForObject(url, GetRecipientDenominationByInternalIdResponseDto.class);
		return GetBasicDataResponseDto.builder().data(response.getTaxId()).build();
	}
}
