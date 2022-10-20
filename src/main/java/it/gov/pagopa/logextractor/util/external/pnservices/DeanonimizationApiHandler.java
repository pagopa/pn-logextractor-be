package it.gov.pagopa.logextractor.util.external.pnservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gov.pagopa.logextractor.util.constant.ExternalServiceConstants;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;
import it.gov.pagopa.logextractor.util.constant.OpensearchConstants;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.PublicAuthorityMappingResponseDto;
import it.gov.pagopa.logextractor.dto.response.SelfCarePaDataResponseDto;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;

/**
 * Uility class for integrations with Piattaforma Notifiche de-anonymization service
 * */
@Component
public class DeanonimizationApiHandler {

	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	@Value("${external.selfcare.getPublicAuthorityName.url}")
	String getPublicAuthorityNameUrl;
	
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
	 * 
	 * @param recipientType      represents the two values of the enum
	 *                           {@link RecipientTypes}.
	 * @param taxId              the tax id of a person
	 * 
	 * @return object of type {@link GetBasicDataResponseDto}, containing the unique
	 *         identifier of a person
	 * @throws LogExtractorException 
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
	public String getUniqueIdentifierForPerson(RecipientTypes recipientType, String taxId) throws LogExtractorException {
		String url = String.format(getUniqueIdURL, recipientType.getValue());
		HttpEntity<String> request =  new HttpEntity<>(taxId);
		String response = client.postForObject(url, request, String.class);
		if(StringUtils.isBlank(response) || "null".equalsIgnoreCase(response)) {
			throw new LogExtractorException("Anonymized tax id is null");
		}
		return response;
	}


	/**
	 * Method that makes a request to Piattaforma Notifiche external service to
	 * retrieve the tax code of a person, given the person's unique identifier
	 * 
	 * @param personId           the unique identifier of a person
	 *                           needs to make a request to Piattaforma Notifiche
	 *                           service
	 * @return object of type {@link GetBasicDataResponseDto}, containing the tax
	 *         code of a person
	 * @throws LogExtractorException 
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
	public GetBasicDataResponseDto getTaxCodeForPerson(String personId) throws LogExtractorException {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(getTaxCodeURL)
		        .queryParam(ExternalServiceConstants.EXT_INTERNAL_ID_PARAM, "{internalId}")
		        .encode()
		        .toUriString();
		Map<String, String> params = new HashMap<>();
		params.put("internalId", personId);
		GetRecipientDenominationByInternalIdResponseDto[] response = client.exchange(
				urlTemplate, 
				HttpMethod.GET,
				entity,
				GetRecipientDenominationByInternalIdResponseDto[].class,
		        params)
				.getBody();
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
	 * @return The list of notifications' general data
	 * @throws LogExtractorException 
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 * */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
	public String getPublicAuthorityId(String publicAuthorityName) throws LogExtractorException {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(selfCareEncodedIpaCodeURL)
		        .queryParam(ExternalServiceConstants.EXT_PA_NAME_PARAM, "{publicAuthorityName}")
		        .encode()
		        .toUriString();
		Map<String, String> params = new HashMap<>();
		params.put("publicAuthorityName", publicAuthorityName);
		PublicAuthorityMappingResponseDto[] response = client.exchange(
				urlTemplate, 
				HttpMethod.GET,
				entity,
				PublicAuthorityMappingResponseDto[].class,
		        params)
				.getBody();
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
	 * @throws LogExtractorException 
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 * */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
	public String getPublicAuthorityName(String publicAuthorityId) throws LogExtractorException {
		String url = String.format(getPublicAuthorityNameUrl, publicAuthorityId); 
		SelfCarePaDataResponseDto response = client.getForEntity(url, SelfCarePaDataResponseDto.class).getBody();		
		if(response == null || StringUtils.isBlank(response.getName()) || "null".equalsIgnoreCase(response.getName())) {
			throw new LogExtractorException("Authority name is null");
		}
		return response.getName();
	}
	
	/** 
	 * Returns the value associated with the specified key.
	 * @param anonymizedDocuments the document list containing the content to write in the output file (.txt, .csv) contained in the output zip archive
	 * @return A list representing the de-anonymized documents 
	 * @throws LogExtractorException 
	 */
	public List<String> deanonimizeDocuments(List<String> anonymizedDocuments, RecipientTypes recipientType) throws LogExtractorException{
		ArrayList<String> deanonymizedDocuments = new ArrayList<>();
		JsonUtilities jsonUtils = new JsonUtilities();
		for(int index=0; index < anonymizedDocuments.size(); index++) {
			String uid = jsonUtils.getValue(anonymizedDocuments.get(index), OpensearchConstants.OS_UID_FIELD);
			String cxId = jsonUtils.getValue(anonymizedDocuments.get(index), OpensearchConstants.OS_CX_ID_FIELD);
			String document = anonymizedDocuments.get(index);
			HashMap<String,String> keyValues = new HashMap<>();
			if(uid != null && !StringUtils.startsWith(uid, "APIKEY-")) {
				GetBasicDataResponseDto taxCodeDto = getTaxCodeForPerson(recipientType.toString() + "-" + uid);
				keyValues.put(OpensearchConstants.OS_UID_FIELD, taxCodeDto.getData());
			}
			if(cxId != null) {
				String publicAuthorityName = null;
				if((StringUtils.startsWithIgnoreCase(cxId, "PF-") || StringUtils.startsWithIgnoreCase(cxId, "PG-"))) {
					publicAuthorityName = getTaxCodeForPerson(cxId).getData();
				}
				if((StringUtils.startsWithIgnoreCase(cxId, "PA-"))) {
					publicAuthorityName = getPublicAuthorityName(cxId);
				}
				keyValues.put(OpensearchConstants.OS_CX_ID_FIELD, publicAuthorityName);
			}
			document = jsonUtils.replaceValues(document, keyValues);
			deanonymizedDocuments.add(document);
		}
		return deanonymizedDocuments;
	}
}
