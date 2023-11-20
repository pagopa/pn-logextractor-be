package it.gov.pagopa.logextractor.util.external.pnservices;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.PublicAuthorityMappingResponseDto;
import it.gov.pagopa.logextractor.dto.response.SelfCarePaDataResponseDto;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;
import it.gov.pagopa.logextractor.util.constant.ExternalServiceConstants;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Uility class for integrations with Piattaforma Notifiche de-anonymization service
 * */
@Slf4j
@Component
public class DeanonimizationApiHandler {

	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	@Value("${external.selfcare.getPublicAuthorityName.url}")
	String getPublicAuthorityNameURL;
	
	@Value("${external.denomination.getRecipientDenominationByInternalId.url}")
	String getTaxCodeURL;
	
	@Value("${external.denomination.ensureRecipientByExternalId.url}")
	String getUniqueIdURL;
	
	@Value("${external.selfcare.getEncodedIpaCode.url}")
	String selfCareEncodedIpaCodeURL;

	/**
	 * Method that makes a request to Piattaforma Notifiche external service to
	 * retrieve the unique identifier of a person, given the recipient type and tax
	 * id of a person
	 * @param recipientType represents the two values of the enum {@link RecipientTypes}.
	 * @param taxId the tax id of a person
	 * @return object of type {@link GetBasicDataResponseDto}, containing the unique identifier of a person
	 * @throws LogExtractorException if the external service response is "null", null or blank
	 */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
	public String getUniqueIdentifierForPerson(RecipientTypes recipientType, String taxId) throws LogExtractorException {
		log.info("Calling getUniqueIdentifierForPerson for {}", "***********");
		String url = String.format(getUniqueIdURL, recipientType.getValue());
		HttpEntity<String> request =  new HttpEntity<>(taxId);
		String response="";
		try {
			response = client.postForObject(url, request, String.class);
		}catch(Exception err) {
			log.error("cannot connect to {}",url);
			throw err;
		}
		if(StringUtils.isBlank(response) || "null".equalsIgnoreCase(response)) {
			throw new LogExtractorException("Anonymized tax id is null");
		}
		return response;
	}


	/**
	 * Method that makes a request to Piattaforma Notifiche external service to
	 * retrieve the tax code of a person, given the person's unique identifier
	 * @param personId the unique identifier of a person needs to make a request to Piattaforma Notifiche service
	 * @return object of type {@link GetBasicDataResponseDto}, containing the tax code of a person
	 * @throws LogExtractorException if the external service response is "null", null, blank or has 0 length
	 */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
	public GetBasicDataResponseDto getTaxCodeForPerson(String personId) throws LogExtractorException {
		log.info("Calling getTaxCodeForPerson for {}", personId);
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(getTaxCodeURL)
		        .queryParam(ExternalServiceConstants.EXT_INTERNAL_ID_PARAM, "{internalId}")
		        .encode()
		        .toUriString();
		Map<String, String> params = new HashMap<>();
		params.put("internalId", personId);
		GetRecipientDenominationByInternalIdResponseDto[] response = null;
		try {
			response = client.exchange(
					urlTemplate, 
					HttpMethod.GET,
					entity,
					GetRecipientDenominationByInternalIdResponseDto[].class,
			        params)
					.getBody();
		}catch(Exception err) {
			log.error("Error decoding personId {}", personId, err);
		}
		if(response == null || response.length == 0 || response[0] == null || StringUtils.isBlank(response[0].getTaxId()) 
				|| "null".equalsIgnoreCase(response[0].getTaxId())) {
			throw new LogExtractorException("Anonymized tax id is null");
		}
		GetBasicDataResponseDto serviceResponse = new GetBasicDataResponseDto();
		serviceResponse.setData(response[0].getTaxId());
		serviceResponse.setMessage(ResponseConstants.SUCCESS_RESPONSE_MESSAGE);
		return serviceResponse;
	}
	
	/**
	 * Performs a GET HTTP request to the Piattaforma Notifiche external service to retrieve the general data of the notifications managed within a period
	 * @param publicAuthorityName the public authority name to get the encoded id for
	 * @return The list of notifications' general data
	 * @throws LogExtractorException if the external service response is "null", null, blank or has 0 length
	 * */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
	public String getPublicAuthorityId(String publicAuthorityName) throws LogExtractorException {
		log.info("Calling getPublicAuthorityId for {}", publicAuthorityName);
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(selfCareEncodedIpaCodeURL)
		        .queryParam(ExternalServiceConstants.EXT_PA_NAME_PARAM, "{publicAuthorityName}")
		        .encode()
		        .toUriString();
		Map<String, String> params = new HashMap<>();
		params.put("publicAuthorityName", publicAuthorityName);
		
		PublicAuthorityMappingResponseDto[] response = null;
		try {
			response = client.exchange(
				urlTemplate, 
				HttpMethod.GET,
				entity,
				PublicAuthorityMappingResponseDto[].class,
		        params)
				.getBody();
		}catch(Exception err) {
			log.error("Error decoding publicAuthorityName {}", publicAuthorityName, err);
		}
		if(response == null || response.length == 0 || response[0] == null || StringUtils.isBlank(response[0].getId()) 
				|| "null".equalsIgnoreCase(response[0].getId())) {
			throw new LogExtractorException("Public authority id is null");
		}
		return response[0].getId();
	}
	
	/**
	 * Performs a GET HTTP request to the Piattaforma Notifiche external service to retrieve the public authority name
	 * @param publicAuthorityId The public authority id
	 * @return The public authority name
	 * @throws LogExtractorException if the external service response is "null", null or blank
	 * */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
	public String getPublicAuthorityName(String publicAuthorityId) throws LogExtractorException {
		log.info("Calling getPublicAuthorityName for {}", publicAuthorityId);
		String url = String.format(getPublicAuthorityNameURL, publicAuthorityId);
		SelfCarePaDataResponseDto response = client.getForEntity(url, SelfCarePaDataResponseDto.class).getBody();		
		if(response == null || StringUtils.isBlank(response.getName()) || "null".equalsIgnoreCase(response.getName())) {
			throw new LogExtractorException("Authority name is null");
		}
		return response.getName();
	}

}
