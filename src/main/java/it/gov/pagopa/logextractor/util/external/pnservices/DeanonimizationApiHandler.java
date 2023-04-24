package it.gov.pagopa.logextractor.util.external.pnservices;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.PublicAuthorityMappingResponseDto;
import it.gov.pagopa.logextractor.dto.response.SelfCarePaDataResponseDto;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.exception.TimeoutException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;
import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.Throttle;
import it.gov.pagopa.logextractor.util.constant.ExternalServiceConstants;
import it.gov.pagopa.logextractor.util.constant.OpensearchConstants;
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
	
	@Value("${external.denomination.timeout:5000}")
	long timeout;
	
	private final Throttle throttle;

	
	public DeanonimizationApiHandler(@Value("${deanonimization.throttle:10}") Integer deanonimizationThrottle) {
		this.throttle = new Throttle(deanonimizationThrottle);
	}
	
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
	 * @param publicAuthorityName the public authority name to get the encoded id for
	 * @return The list of notifications' general data
	 * @throws LogExtractorException if the external service response is "null", null, blank or has 0 length
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
	 * @throws LogExtractorException if the external service response is "null", null or blank
	 * */
	@Cacheable(cacheNames="Cluster", cacheManager = "cacheManager10Hour")
	public String getPublicAuthorityName(String publicAuthorityId) throws LogExtractorException {
		String url = String.format(getPublicAuthorityNameURL, publicAuthorityId);
		SelfCarePaDataResponseDto response = client.getForEntity(url, SelfCarePaDataResponseDto.class).getBody();		
		if(response == null || StringUtils.isBlank(response.getName()) || "null".equalsIgnoreCase(response.getName())) {
			throw new LogExtractorException("Authority name is null");
		}
		return response.getName();
	}
	
	/** 
	 * Returns the value associated with the specified key.
	 * @param recipientType the entity's recipient type
	 * @param anonymizedDocuments the document list containing the content to write in the output
	 *                               file (.txt, .csv) contained in the output zip archive
	 * @return false if it cannot handle request, true otherwise 
	 * @throws LogExtractorException if the external service response is "null", null or blank
	 * @throws JsonProcessingException 
	 */
	public boolean deanonimizeDocuments(File anonymizedDocuments, RecipientTypes recipientType, OutputStream out) throws LogExtractorException, JsonProcessingException, TimeoutException {
		
		long start = new Date().getTime();
		while(!throttle.acceptRequest()) {
			if (new Date().getTime() - start > timeout) {
				log.warn("Timeout exceeded for Deanonimization service!");
				throw new TimeoutException();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				//Do nothing
			}
		}
		
		ObjectMapper mapper = new ObjectMapper();
		JsonUtilities jsonUtils = new JsonUtilities();
		Map<String, String> keyValues = new HashMap<>();
		BufferedReader br = null;
		BufferedWriter wr = null;
		FileReader fr = null;
		try {
			fr = new FileReader(anonymizedDocuments);
			br = new BufferedReader(fr);
			wr = new BufferedWriter(new OutputStreamWriter(out));
			
			String currentDocument;
			while ((currentDocument = br.readLine()) != null) {
					JsonNode root = mapper.readTree(currentDocument);
					JsonNode uid = root.get(OpensearchConstants.OS_UID_FIELD);
					JsonNode cxId = root.get(OpensearchConstants.OS_CX_ID_FIELD);
	
					if (uid != null && !uid.asText().startsWith("APIKEY-")) {
						GetBasicDataResponseDto taxCodeDto = getTaxCodeForPerson(
								recipientType.toString() + "-" + uid.asText());
						keyValues.put(OpensearchConstants.OS_UID_FIELD, taxCodeDto.getData());
					}
					if (cxId != null) {
						String deanonimizedIdentifier = null;
						if (cxId.asText().startsWith("PF-") || cxId.asText().startsWith("PG-")) {
							deanonimizedIdentifier = getTaxCodeForPerson(cxId.asText()).getData();
						} else if (cxId.asText().startsWith("PA-")) {
							deanonimizedIdentifier = getPublicAuthorityName(cxId.asText());
						}
						keyValues.put(OpensearchConstants.OS_CX_ID_FIELD, deanonimizedIdentifier);
					}
					wr.write(jsonUtils.replaceValues(currentDocument, keyValues));
					wr.newLine();
					wr.flush();
					currentDocument=null;
			}
			return true;
		} catch (Exception e) {
			log.error("Error reading {}", anonymizedDocuments.getName(), e);
		} finally {
			if (br!=null) {
				IOUtils.closeQuietly(br);
			}
			if (fr != null) {
				IOUtils.closeQuietly(fr);
			}
		}
		return false;
		
	}
}
