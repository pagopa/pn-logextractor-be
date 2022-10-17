package it.gov.pagopa.logextractor.annotation.validator;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;

import java.util.Arrays;

/**
 * Validation class for {@link RecipientType} custom annotation
 * */
public class RecipientTypeValidator implements ConstraintValidator<RecipientType, String>{

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value != null && Arrays.asList(RecipientTypes.values()).contains(value);
	}
}
