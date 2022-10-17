package it.gov.pagopa.logextractor.annotation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import it.gov.pagopa.logextractor.annotation.PersonLogsFields;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonLogsRequestDto;

/**
 * Validation class for {@link PersonLogsFields} custom annotation
 * */
public class PersonLogsFieldsValidator implements ConstraintValidator<PersonLogsFields, PersonLogsRequestDto>{
	@Override
	public boolean isValid(PersonLogsRequestDto value, ConstraintValidatorContext context) {
		
		// use case 3
		if(value.getTaxId() != null) {
		    return value.getDateFrom() != null && value.getDateTo() != null;
		}
		
		// use case 4 and 8
		else if(value.getIun() != null) {
			return true;
		}	
		
		// use case 7
		else if(value.getPersonId() != null) {
			return value.getDateFrom() != null && value.getDateTo() != null;
		}		
		
		else{
			return false;
		}
	}
}
