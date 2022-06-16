package it.gov.pagopa.logextractor.annotation.validator;

import java.time.LocalDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import it.gov.pagopa.logextractor.annotation.PeriodOf3Months;
import it.gov.pagopa.logextractor.dto.request.PersonLogsRequestDto;

public class PeriodOf3MonthsValidator implements ConstraintValidator<PeriodOf3Months, PersonLogsRequestDto> {

	@Override
	public boolean isValid(PersonLogsRequestDto value, ConstraintValidatorContext context) {
		
		if (value.getDateFrom() == null && value.getDateFrom() == null) {
			return true;
		}
		
		LocalDate maxEndDate = LocalDate.parse(value.getDateFrom()).plusMonths(3);
		LocalDate startDate = LocalDate.parse(value.getDateFrom());
		LocalDate endDate = LocalDate.parse(value.getDateTo());
		return endDate.compareTo(startDate) >= 0 && endDate.compareTo(maxEndDate) <= 0 && endDate.compareTo(LocalDate.now()) <= 0;
	}
}
