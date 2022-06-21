package it.gov.pagopa.logextractor.service;
import java.io.IOException;
import java.text.ParseException;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.util.RecipientTypes;

public interface LogService {
	DownloadArchiveResponseDto getAnonymizedPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun, String personId) throws IOException;
	DownloadArchiveResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode) throws IOException, ParseException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException;
	DownloadArchiveResponseDto getTraceIdLogs(String dateFrom, String dateTo, String traceId) throws IOException;
	DownloadArchiveResponseDto getDeanonymizedPersonLogs(RecipientTypes recipientType, String dateFrom, String dateTo, String ticketNumber, String taxid,String iun) throws IOException;
	DownloadArchiveResponseDto getNotificationInfoLogs(String iun) throws IOException;
}
