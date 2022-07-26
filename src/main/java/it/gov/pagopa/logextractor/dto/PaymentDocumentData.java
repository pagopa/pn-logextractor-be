package it.gov.pagopa.logextractor.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@AllArgsConstructor
public class PaymentDocumentData {
	private Integer recipients;
	private Map<String, String> paymentKeys;
	
	@Override
	public String toString() {
		return "payment="+paymentKeys.toString();
	}
}
