package it.gov.pagopa.logextractor.util.external.safestorage;

import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SafeStorageClient {
	@Value("${external.safeStorage.downloadFile.url}")
	String downloadFileURL;
	
	@Value("${external.safeStorage.baseUrl:}")
	String safeStorageBaseUrl;
	
	@Value("${external.safeStorage.downloadFile.stage}")
	String safeStorageStage;
	
	@Value("${external.safeStorage.downloadFile.cxId}")
	String safeStorageCxid;
	
	
	@Autowired
	@Qualifier("simpleRestTemplate")
	RestTemplate client;

	public SafeStorageCreateFileResponse createFle(byte[] content) {
		String url = safeStorageBaseUrl+ "/safe-storage/v1/files";
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.set("x-pagopa-safestorage-cx-id", safeStorageCxid);
		List<MediaType> acceptedTypes = new ArrayList<>();
		acceptedTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptedTypes);
		
		/*requestHeaders.add("x-checksum", "SHA-256");
		
		String sha256 = computeSha256(content);
       	requestHeaders.add("x-checksum-value", sha256);*/

        
		HttpEntity<String> entity = new HttpEntity<>("{"
				+ "  \"contentType\": \"application/zip\","
				+ "  \"documentType\": \"PN_LOG_EXTRACTOR_RESULT\","
				+ "  \"status\":\"PRELOADED\""
				+ "}"
				,requestHeaders);
		String resp = client.exchange(url, HttpMethod.POST, entity, String.class).getBody();
		
		
		log.info("SafeStorage create file response: {}",resp);
		
		JSONObject jObject  = new JSONObject(resp); 
		String uploadUrl = jObject.getString("uploadUrl"); 
		String key = jObject.getString("key");

		SafeStorageCreateFileResponse ret = new SafeStorageCreateFileResponse();
		ret.setKey(key);
		ret.setSecret(jObject.getString("secret"));
		ret.setUploadUrl(uploadUrl);
		return ret;
	}
	
	public void uploadToSafeStorage(SafeStorageCreateFileResponse createFileResp, byte[] content) {
		
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-type", "application/zip");
        headers.add("x-amz-checksum-sha256", computeSha256(content));
        headers.add("x-amz-meta-secret", createFileResp.getSecret());
        
        
        HttpEntity<Resource> req = new HttpEntity<>(new ByteArrayResource(content), headers);
        org.springframework.http.ResponseEntity<String> res = client.exchange(URI.create(createFileResp.getUploadUrl()), HttpMethod.PUT, req, String.class);

        log.info("SafeStorage upload file response-code: {}", res.getStatusCodeValue());
        
	}
	
	public String getDownloadUrl(String key) {

        String url = safeStorageBaseUrl+ "/safe-storage/v1/files/"+key;
        HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.set("x-pagopa-safestorage-cx-id", safeStorageCxid);
		List<MediaType> acceptedTypes = new ArrayList<>();
		acceptedTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptedTypes);
        HttpEntity<String> emptyEntity = new HttpEntity<>("", requestHeaders);
        org.springframework.http.ResponseEntity<String> res = client.exchange(url, HttpMethod.GET, emptyEntity, String.class);

        log.info("SafeStorage downloadfile response: {}", res.getBody());
        
        JSONObject jObject  = new JSONObject(res.getBody());
        return jObject.getJSONObject("download").getString("url");
	}
	/*{
		 try {
	            
	            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
	            headers.add("Content-type", fileCreationRequest.getContentType());
	            headers.add("x-amz-checksum-sha256", sha256);
	            headers.add("x-amz-meta-secret", fileCreationResponse.getSecret());

	            HttpEntity<Resource> req = new HttpEntity<>(new ByteArrayResource(fileCreationRequest.getContent()), headers);
	            
	            URI url = URI.create(fileCreationResponse.getUploadUrl());
	            HttpMethod method = fileCreationResponse.getUploadMethod() == FileCreationResponse.UploadMethodEnum.POST ? HttpMethod.POST : HttpMethod.PUT;
	            
	            ResponseEntity<String> res = restTemplate.exchange(url, method, req, String.class);
	            
	            if (res.getStatusCodeValue() != org.springframework.http.HttpStatus.OK.value())
	            {
	                throw new ExternalChannelsMockException("File upload failed");
	            }
	        } catch (PnInternalException ee)
	        {
	            log.error("uploadContent PnInternalException uploading file", ee);
	            throw ee;
	        }
	        catch (Exception ee)
	        {
	            log.error("uploadContent Exception uploading file", ee);
	            throw new ExternalChannelsMockException("Exception uploading file", ee);
	        }
	}*/
	
	/*private void createAndUploadContentSafeStorage(FileCreationWithContentRequest fileCreationRequest) {
        try {
            log.debug("Start call createAndUploadContentSafeStorage - documentType={} filesize={}", fileCreationRequest.getDocumentType(), fileCreationRequest.getContent().length);

            String sha256 = computeSha256(fileCreationRequest.getContent());

//            FileCreationResponse fileCreationResponse = safeStorageClient.createFile(fileCreationRequest, sha256);

//            FileCreationResponseInt fileCreationResponseInt = uploadContent(fileCreationRequest, sha256, fileCreationResponse);

            log.info("createAndUploadContentSafeStorage file uploaded successfully key={} sha256={}", fileCreationResponseInt.getKey(), sha256);

//            return fileCreationResponseInt;
        } catch (Exception e) {
            log.error("Error creating file" , e);
        }
    }*/
	
	
	/*
	
	private org.springframework.http.ResponseEntity<> createFileWithHttpInfo(String xPagopaSafestorageCxId, String xChecksum, String xChecksumValue, FileCreationRequest fileCreationRequest) throws RestClientException {
        Object postBody = fileCreationRequest;
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        if (xPagopaSafestorageCxId != null)
        headerParams.add("x-pagopa-safestorage-cx-id", xPagopaSafestorageCxId);
        if (xChecksum != null)
        headerParams.add("x-checksum", xChecksum);
        if (xChecksumValue != null)
        headerParams.add("x-checksum-value", xChecksumValue);

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] contentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] { "ApiKeyAuth" };

        ParameterizedTypeReference<FileCreationResponse> returnType = new ParameterizedTypeReference<FileCreationResponse>() {};
        return apiClient.invokeAPI("/safe-storage/v1/files", HttpMethod.POST, Collections.<String, Object>emptyMap(), queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, authNames, returnType);
    }
	*/
	 private String computeSha256( byte[] content ) {
	        try{
	            MessageDigest digest = MessageDigest.getInstance("SHA-256");
	            byte[] encodedHash = digest.digest( content );
	            return Base64Utils.encodeToString( encodedHash );
	        } catch (Exception e) {
	            log.error("Cannot compute sha256", e);
	            return null;
	        }
	    }
}
