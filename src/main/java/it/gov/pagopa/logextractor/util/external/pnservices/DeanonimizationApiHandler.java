package it.gov.pagopa.logextractor.util.external.pnservices;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.dto.request.EnsureRecipientByExternalIdRequestDto;
import it.gov.pagopa.logextractor.dto.response.EnsureRecipientByExternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.SelfCarePaDataResponseDto;
import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.RecipientTypes;

/**
 * Uility class for integrations with Piattaforma Notifiche de-anonymization service
 * */
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
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
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
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 */
	@Cacheable(cacheNames="services")
	public GetBasicDataResponseDto getTaxCodeForPerson(String personId, String externalServiceUrl) {
		String url = String.format(externalServiceUrl, personId);
		GetRecipientDenominationByInternalIdResponseDto response = client.getForObject(url, GetRecipientDenominationByInternalIdResponseDto.class);
		return GetBasicDataResponseDto.builder().data(response.getTaxId()).build();
	}
	
	/**
	 * Performs a GET HTTP request to the PN external service to retrieve the general data of the notifications managed within a period
	 * @param url The PN external service base URL
	 * @param ipaCode The public authority code
	 * @return The list of notifications' general data
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 * */
	@Cacheable(cacheNames="services")
	public String getEncodedIpaCode(String url, String ipaCode) {
        ResponseEntity<String> response = client.getForEntity(url, String.class);
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
	 * @param serviceUrl The external pn service URL
	 * @return The public authority name
	 * @throws {@link HttpServerErrorException}
	 * @throws {@link HttpClientErrorException}
	 * */
	@Cacheable(cacheNames="services")
	public String getPublicAuthorityName(String publicAuthorityId, String serviceUrl) {
		String url = String.format(serviceUrl, publicAuthorityId); 
		ResponseEntity<SelfCarePaDataResponseDto> response = client.getForEntity(url, SelfCarePaDataResponseDto.class);
		return response.getBody().getName();
	}
	
	/** 
	 * Returns the value associated with the specified key.
	 * @param anonymizedDocument the document containing the content to write in the output file (.txt, .csv) contained in the output zip archive
	 * @param getTaxCodeURL the url of de-anonymization service
	 * @return A list representing the de-anonymized documents 
	 */
	public ArrayList<String> toDeanonymizedDocuments(ArrayList<String> anonymizedDocuments, String getTaxCodeURL, String getPublicAuthorityNameUrl){
		ArrayList<String> deanonymizedDocuments = new ArrayList<String>();
		for(int index=0; index<anonymizedDocuments.size(); index++) {
			String uuid = JsonUtilities.getValue(anonymizedDocuments.get(index), "uid");
			String cxId = JsonUtilities.getValue(anonymizedDocuments.get(index), "cx_id");
			String document = anonymizedDocuments.get(index);
			HashMap<String,String> keyValues = new HashMap<String,String>();
			if(uuid != null) {
				GetBasicDataResponseDto taxCodeDto = getTaxCodeForPerson(uuid, getTaxCodeURL);
				keyValues.put("uid", taxCodeDto.getData());
			}
			if(cxId != null) {
				String publicAuthorityName = getPublicAuthorityName(cxId, getPublicAuthorityNameUrl);
				keyValues.put("cx_id", publicAuthorityName);
			}
			document = JsonUtilities.replaceValues(document, keyValues);
			deanonymizedDocuments.add(document);
		}
		return deanonymizedDocuments;
	}
}
