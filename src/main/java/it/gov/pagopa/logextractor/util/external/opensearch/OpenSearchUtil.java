package it.gov.pagopa.logextractor.util.external.opensearch;

import java.util.ArrayList;
import java.util.HashMap;

import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;
import it.gov.pagopa.logextractor.util.json.JsonUtilities;

public class OpenSearchUtil {
	
	/** 
	 * Returns the value associated with the specified key.
	 * @param anonymizedDocument the document containing the content to write in the output file (.txt, .csv) contained in the output zip archive
	 * @param getTaxCodeURL the url of deanonymization service
	 * @return deanonymizedDocuments the deanonymized documents 
	 */
	public static ArrayList<String> toDeanonymizedDocuments(ArrayList<String> anonymizedDocuments, String getTaxCodeURL){
		DeanonimizationApiHandler handler = new DeanonimizationApiHandler();
		ArrayList<String> deanonymizedDocuments = new ArrayList<String>();
		for(int i=0; i<anonymizedDocuments.size(); i++) {
			String uuid = JsonUtilities.getValue(anonymizedDocuments.get(i), "uid");
			String cxId = JsonUtilities.getValue(anonymizedDocuments.get(i), "cx_id");
			String document = anonymizedDocuments.get(i);
			HashMap<String,String> keyValues = new HashMap<String,String>() ;
			if(uuid != null) {
				GetBasicDataResponseDto taxCodeDto = handler.getTaxCodeForPerson(uuid, getTaxCodeURL);
				keyValues.put("uid", taxCodeDto.getData());
			}
			if(cxId != null) {
				GetBasicDataResponseDto NotificationDto = handler.getTaxCodeForPerson(cxId, getTaxCodeURL);//TODO: add the url string for cx_id deanonymization service
				keyValues.put("cx_id", NotificationDto.getData());
			}
			document = JsonUtilities.deanonymizeDocument(document, keyValues);
			deanonymizedDocuments.add(document);
		}
		return deanonymizedDocuments;
	}
}
