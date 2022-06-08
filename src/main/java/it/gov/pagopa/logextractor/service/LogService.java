package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.text.ParseException;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.response.PasswordResponseDto;
import net.lingala.zip4j.ZipFile;


public interface LogService {
	byte[] getPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun, String personId, String password) throws IOException;
	byte[] getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode, String password) throws IOException, ParseException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException;
	PasswordResponseDto createPassword();
}
