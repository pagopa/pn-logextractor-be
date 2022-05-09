package it.gov.pagopa.logextractor.annotation.validator;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import it.gov.pagopa.logextractor.annotation.RecipientType;
import it.gov.pagopa.logextractor.util.RecipientTypes;

public class RecipientTypeValidator implements ConstraintValidator<RecipientType, String>{

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value != null && RecipientTypes.isValid(value) ;
	}
}
