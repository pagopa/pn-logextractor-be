package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.text.ParseException;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.response.PasswordResponseDto;


public interface LogService {
	PasswordResponseDto getPersonLogs(String dateFrom, String dateTo, String referenceDate, String ticketNumber, Integer uin, String personId, String password) throws IOException;
	PasswordResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode) throws IOException, ParseException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException;
	PasswordResponseDto createPassword();
}
