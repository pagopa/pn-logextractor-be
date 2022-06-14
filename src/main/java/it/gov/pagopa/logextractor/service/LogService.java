package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.text.ParseException;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.request.DownloadArchiveResponseDto;

/**
 * An interface containing all methods for logs
 */
public interface LogService {

	/**
	 * Service method that makes requests to OpenSearch to retrieve the logs of a
	 * person in a zip archive, locked with a password.
	 * 
	 * @param dateFrom     start date of a period in which the log can be
	 * @param dateTo       end date of a period in which the log can be
	 * @param ticketNumber the ticket number of a person
	 * @param iun          the unique identifier of a notification
	 * @param personId     the unique identifier of a person
	 * @return {@link DownloadArchiveResponseDto} containing the zip archive and the
	 *         password for it
	 * @throws IOException
	 */
	DownloadArchiveResponseDto getPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun,
			String personId) throws IOException;

	DownloadArchiveResponseDto getMonthlyNotifications(String ticketNumber, String referenceMonth, String ipaCode)
			throws IOException, ParseException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException;

	DownloadArchiveResponseDto getTraceIdLogs(String dateFrom, String dateTo, String traceId) throws IOException;

	/**
	 * Service method that makes requests to OpenSearch to retrieve the logs of a
	 * notification in a zip archive, locked with a password.
	 * 
	 * @param ticketNumber the ticket number of a person
	 * @return {@link DownloadArchiveResponseDto} containing the zip archive and the
	 *         password for it
	 * @throws IOException
	 */
	DownloadArchiveResponseDto getNotificationInfoLogs(String iun) throws IOException;
}
