package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.text.ParseException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.dto.request.DownloadArchiveResponseDto;


public interface LogService {
	DownloadArchiveResponseDto getPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun, String personId) throws IOException;
	DownloadArchiveResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode) throws IOException, ParseException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException;
}
