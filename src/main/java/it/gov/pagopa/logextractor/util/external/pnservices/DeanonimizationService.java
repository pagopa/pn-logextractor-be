package it.gov.pagopa.logextractor.util.external.pnservices;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;
import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.constant.OpensearchConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeanonimizationService {

	@Autowired
	DeanonimizationApiHandler apiHandler;
	
	/** 
	 * Returns the value associated with the specified key.
	 * @param recipientType the entity's recipient type
	 * @param anonymizedDocuments the document list containing the content to write in the output
	 *                               file (.txt, .csv) contained in the output zip archive
	 * @return A list representing the de-anonymized documents 
	 * @throws LogExtractorException if the external service response is "null", null or blank
	 * @throws JsonProcessingException 
	 */
	public void deanonimizeDocuments(File anonymizedDocuments, RecipientTypes recipientType, OutputStream out) throws LogExtractorException, JsonProcessingException {
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
	
					//log.info("deanonimize doc with uid: {} and cxId: {}", uid, cxId);
					
					
					if (uid != null && !uid.asText().startsWith("APIKEY-")) {
						GetBasicDataResponseDto taxCodeDto = apiHandler.getTaxCodeForPerson(
								recipientType.toString() + "-" + uid.asText());
						keyValues.put(OpensearchConstants.OS_UID_FIELD, taxCodeDto.getData());
					}
					if (cxId != null) {
						String deanonimizedIdentifier = null;
						if (cxId.asText().startsWith("PF-") || cxId.asText().startsWith("PG-")) {
							deanonimizedIdentifier = apiHandler.getTaxCodeForPerson(cxId.asText()).getData();
						} else if (cxId.asText().startsWith("PA-")) {
							deanonimizedIdentifier = apiHandler.getPublicAuthorityName(cxId.asText());
						}
						keyValues.put(OpensearchConstants.OS_CX_ID_FIELD, deanonimizedIdentifier);
					}
					wr.write(jsonUtils.replaceValues(currentDocument, keyValues));
					wr.newLine();
					wr.flush();
					currentDocument=null;
			}
		} catch (Exception e) {
			log.error("IR-"+e.getMessage());
			log.error("Error reading {}", anonymizedDocuments.getName(), e);
		} finally {
			if (br!=null) {
				IOUtils.closeQuietly(br);
			}
			if (fr != null) {
				IOUtils.closeQuietly(fr);
			}
		}
		
	}
	
	public String getUniqueIdentifierForPerson(RecipientTypes recipientType, String taxId) throws LogExtractorException {
		return apiHandler.getUniqueIdentifierForPerson(recipientType, taxId);
	}
	
	public String getPublicAuthorityId(String publicAuthorityName) throws LogExtractorException {
		return apiHandler.getPublicAuthorityId(publicAuthorityName);
	}
}
