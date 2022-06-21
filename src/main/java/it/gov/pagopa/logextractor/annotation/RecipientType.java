package it.gov.pagopa.logextractor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import it.gov.pagopa.logextractor.annotation.validator.RecipientTypeValidator;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RecipientTypeValidator.class)
public @interface RecipientType {

	String message() default "Invalid recipient type value";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
