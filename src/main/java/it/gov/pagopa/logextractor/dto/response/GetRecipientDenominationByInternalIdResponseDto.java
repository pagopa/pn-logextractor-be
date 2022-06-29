package it.gov.pagopa.logextractor.dto.response;

import java.io.Serializable;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetRecipientDenominationByInternalIdResponseDto implements Serializable {
	private static final long serialVersionUID = 1L;

	@Size(min = 16, max = 16)
	@Pattern(regexp = Constants.FISCAL_CODE_PATTERN, message = "Invalid Tax ID")
	private String taxId;

	@Size(min = 1, max = 100, message = "Invalid Intenal ID")
	@Pattern(regexp = Constants.INTERNAL_ID_PATTERN)
	private String internalId;

	@RecipientType
	private String recipientType;

	@Pattern(regexp = Constants.ALPHA_NUMERIC_WITHOUT_SPECIAL_CHAR_PATTERN, message = "Invalid denomination")
	private String denomination;
}