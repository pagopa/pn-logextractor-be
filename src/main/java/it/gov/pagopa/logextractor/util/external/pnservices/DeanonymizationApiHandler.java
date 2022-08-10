package it.gov.pagopa.logextractor.util.external.pnservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.PublicAuthorityMappingResponseDTO;
import it.gov.pagopa.logextractor.dto.response.SelfCarePaDataResponseDto;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.Constants;
import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.ResponseConstants;

/**
 * Uility class for integrations with Piattaforma Notifiche de-anonymization service
 * */
@Component
public class DeanonymizationApiHandler {

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
	 *                           {@link RecipientType}.
	 * @param taxId              the tax id of a person
	 * 
	 * @return object of type {@link GetBasicDataResponseDto}, containing the unique
	 *         identifier of a person
	 * @throws LogExtractorException 
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
//	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager1Minute")
	public String getUniqueIdentifierForPerson(RecipientTypes recipientType, String taxId) throws LogExtractorException {
		String url = String.format(getUniqueIdURL, recipientType.toString());
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
	 * @param externalServiceUrl he url of the external endpoint that the method
	 *                           needs to make a request to Piattaforma Notifiche
	 *                           service
	 * @return object of type {@link GetBasicDataResponseDto}, containing the tax
	 *         code of a person
	 * @throws LogExtractorException 
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
//	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager1Minute")
	public GetBasicDataResponseDto getTaxCodeForPerson(String personId) throws LogExtractorException {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(getTaxCodeURL)
		        .queryParam(Constants.EXT_INTERNAL_ID_PARAM, "{internalId}")
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
		if(response == null || response[0] == null || StringUtils.isBlank(response[0].getTaxId()) 
				|| "null".equalsIgnoreCase(response[0].getTaxId())) {
			throw new LogExtractorException("Anonymized tax id is null");
		}
		return GetBasicDataResponseDto.builder().data(response[0].getTaxId()).message(ResponseConstants.SUCCESS_RESPONSE_MESSAGE).build();
	}
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the general data of the notifications managed within a period
	 * @param ipaCode The public authority code
	 * @return The list of notifications' general data
	 * @throws LogExtractorException 
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 * */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
//	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager1Minute")
	public String getPublicAuthorityId(String publicAuthorityName) throws LogExtractorException {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(selfCareEncodedIpaCodeURL)
		        .queryParam(Constants.EXT_PA_NAME_PARAM, "{publicAuthorityName}")
		        .encode()
		        .toUriString();
		Map<String, String> params = new HashMap<>();
		params.put("publicAuthorityName", publicAuthorityName);
		PublicAuthorityMappingResponseDTO[] response = client.exchange(
				urlTemplate, 
				HttpMethod.GET,
				entity,
				PublicAuthorityMappingResponseDTO[].class,
		        params)
				.getBody();
		if(response == null || response[0] == null || StringUtils.isBlank(response[0].getId()) 
				|| "null".equalsIgnoreCase(response[0].getId())) {
			throw new LogExtractorException("Public authority id is null");
		}
		return response[0].getId();
	}
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the public authority name
	 * @param publicAuthorityId The public authority id
	 * @return The public authority name
	 * @throws LogExtractorException 
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 * */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
//	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager1Minute")
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
	 * @param anonymizedDocument the document containing the content to write in the output file (.txt, .csv) contained in the output zip archive
	 * @param getTaxCodeURL the url of de-anonymization service
	 * @return A list representing the de-anonymized documents 
	 * @throws LogExtractorException 
	 */
	public List<String> deanonymizeDocuments(List<String> anonymizedDocuments, RecipientTypes recipientType) throws LogExtractorException{
		ArrayList<String> deanonymizedDocuments = new ArrayList<>();
		for(int index=0; index<anonymizedDocuments.size(); index++) {
			String uuid = JsonUtilities.getValue(anonymizedDocuments.get(index), Constants.OS_UID_FIELD);
			String cxId = JsonUtilities.getValue(anonymizedDocuments.get(index), Constants.OS_CX_ID_FIELD);
			String document = anonymizedDocuments.get(index);
			HashMap<String,String> keyValues = new HashMap<>();
			if(uuid != null && !StringUtils.startsWith(uuid, "APIKEY-")) {
				GetBasicDataResponseDto taxCodeDto = getTaxCodeForPerson(recipientType.toString() + "-" + uuid);
				keyValues.put(Constants.OS_UID_FIELD, taxCodeDto.getData());
			}
			if(cxId != null) {
				String publicAuthorityName = null;
				if((StringUtils.startsWithIgnoreCase(cxId, "PF-") || StringUtils.startsWithIgnoreCase(cxId, "PG-"))) {
					publicAuthorityName = getTaxCodeForPerson(cxId).getData();
				}
				if((StringUtils.startsWithIgnoreCase(cxId, "PA-"))) {
					publicAuthorityName = getPublicAuthorityName(cxId);
				}
				keyValues.put(Constants.OS_CX_ID_FIELD, publicAuthorityName);
			}
			document = JsonUtilities.replaceValues(document, keyValues);
			deanonymizedDocuments.add(document);
		}
		return deanonymizedDocuments;
	}
}
