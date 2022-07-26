package it.gov.pagopa.logextractor.util.external.pnservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.SelfCarePaDataResponseDto;
import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import lombok.extern.slf4j.Slf4j;

/**
 * Uility class for integrations with Piattaforma Notifiche de-anonymization service
 * */
@Component
@Slf4j
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
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
//	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager1Minute")
	public String getUniqueIdentifierForPerson(RecipientTypes recipientType, String taxId) {
		String url = String.format(getUniqueIdURL, recipientType.toString());
		HttpEntity<String> request =  new HttpEntity<String>(taxId);
		String response = client.postForObject(url, request, String.class);
		log.info("Anonymized data: {}", response);
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
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
//	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager1Minute")
	public GetBasicDataResponseDto getTaxCodeForPerson(String personId) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(getTaxCodeURL)
		        .queryParam("internalId", "{internalId}")
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
		return GetBasicDataResponseDto.builder().data(response[0].getTaxId()).build();
	}
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the general data of the notifications managed within a period
	 * @param ipaCode The public authority code
	 * @return The list of notifications' general data
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 * */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
//	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager1Minute")
	public String getEncodedIpaCode(String ipaCode) {
        ResponseEntity<String> response = client.getForEntity(selfCareEncodedIpaCodeURL, String.class);
        return getIpaCode(response.getBody(), ipaCode);
	}
	
	/**
	 * Extracts the public authority encoded code from the pn service response
	 * @param pnResponse The PN external service response
	 * @param ipaCode The public authority code
	 * @return The public authority id
	 * */
	public String getIpaCode(String pnResponse, String ipaCode) {
		JSONArray jsonResponse = new JSONArray(pnResponse);
		for(int index = 0; index < jsonResponse.length(); index++) {
			JSONObject currentObj = jsonResponse.getJSONObject(index);
			if(ipaCode.equals(currentObj.getString("name"))) {
				return currentObj.getString("id");
			}
		}
		return null;
	}
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the public authority name
	 * @param publicAuthorityId The public authority id
	 * @return The public authority name
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 * */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
//	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager1Minute")
	public String getPublicAuthorityName(String publicAuthorityId) {
		String url = String.format(getPublicAuthorityNameUrl, publicAuthorityId); 
		ResponseEntity<SelfCarePaDataResponseDto> response = client.getForEntity(url, SelfCarePaDataResponseDto.class);
		return response.getBody().getName();
	}
	
	/** 
	 * Returns the value associated with the specified key.
	 * @param anonymizedDocument the document containing the content to write in the output file (.txt, .csv) contained in the output zip archive
	 * @param getTaxCodeURL the url of de-anonymization service
	 * @return A list representing the de-anonymized documents 
	 */
	public ArrayList<String> deanonymizeDocuments(ArrayList<String> anonymizedDocuments, RecipientTypes recipientType){
		ArrayList<String> deanonymizedDocuments = new ArrayList<String>();
		for(int index=0; index<anonymizedDocuments.size(); index++) {
			String uuid = JsonUtilities.getValue(anonymizedDocuments.get(index), "uid");
			String cxId = JsonUtilities.getValue(anonymizedDocuments.get(index), "cx_id");
			String document = anonymizedDocuments.get(index);
			HashMap<String,String> keyValues = new HashMap<String,String>();
			if(uuid != null && !StringUtils.startsWith(uuid, "APIKEY-")) {
				GetBasicDataResponseDto taxCodeDto = getTaxCodeForPerson(recipientType.toString() + "-" + uuid);
				keyValues.put("uid", taxCodeDto.getData());
			}
			if(cxId != null) {
				String publicAuthorityName = null;
				if((StringUtils.startsWithIgnoreCase(cxId, "PF-") || StringUtils.startsWithIgnoreCase(cxId, "PG-"))) {
					publicAuthorityName = getTaxCodeForPerson(cxId).getData();
				}
				if((StringUtils.startsWithIgnoreCase(cxId, "PA-"))) {
					publicAuthorityName = getPublicAuthorityName(cxId);
				}
				keyValues.put("cx_id", publicAuthorityName);
			}
			document = JsonUtilities.replaceValues(document, keyValues);
			deanonymizedDocuments.add(document);
		}
		return deanonymizedDocuments;
	}
}
