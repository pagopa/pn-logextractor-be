package it.gov.pagopa.logextractor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import it.gov.pagopa.logextractor.annotation.validator.PersonLogsFieldsValidator;

/**
 * Custom annotation to validatethe PersonLogsRequestDto 
 * */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PersonLogsFieldsValidator.class)
public @interface PersonLogsFields {
String message() default "Required fileds not found";
	
	Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
