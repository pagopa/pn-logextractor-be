package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.text.ParseException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.*;

public interface LogService {
	
	/**
	 * Service method that retrieves the anonymized logs related to a person's activities history in a period
	 * or to a notification's activities history within 3 months from its legal start date
	 * @param requestData the input data of type {@link PersonLogsRequestDto}
	 * @return {@link BaseResponseDto} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException in case of an IO error
	 */
	BaseResponseDto getAnonymizedPersonLogs(PersonLogsRequestDto requestData, String xPagopaHelpdUid) throws IOException;
	
	/**
	 * Service method that retrieves informations about the notifications sent by a public authority in a specific month
	 * @param requestData the input data of type {@link MonthlyNotificationsRequestDto}
	 * @return A byte array representation of the output zip archive and the password to access its files
	 * @throws IOException in case of an IO error
	 * @throws ParseException in case of a parse error
	 * @throws CsvDataTypeMismatchException in case csv data types are mismatching during the csv file writing process
	 * @throws CsvRequiredFieldEmptyException in case of any required field is missing
	 * @throws LogExtractorException in case of a business logic error
	 */
	BaseResponseDto getMonthlyNotifications(MonthlyNotificationsRequestDto requestData, String xPagopaHelpdUid) throws IOException, ParseException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, LogExtractorException;
	
	/**
	 * Service method that retrieves the anonymized logs belonging to the same process within a period
	 * @param requestData the input data of type {@link TraceIdLogsRequestDto}
	 * @return {@link BaseResponseDto} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException in case of an IO error
	 * @throws LogExtractorException in case of a business logic error
	 */
	BaseResponseDto getTraceIdLogs(TraceIdLogsRequestDto requestData, String xPagopaHelpdUid) throws IOException, LogExtractorException;
	
	/**
	 * Service method that retrieves the de-anonymized logs related to a person's activities history in a period
	 * or to a notification's activities history within 3 months from its legal start date
	 * @param requestData the input data of type {@link PersonLogsRequestDto}
	 * @param xPagopaHelpdUid 
	 * @return {@link BaseResponseDto} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException in case of an IO error
	 * @throws LogExtractorException in case of a business logic error
	 */
	BaseResponseDto getDeanonimizedPersonLogs(PersonLogsRequestDto requestData, String xPagopaHelpdUid) throws IOException, LogExtractorException;
	
	/**
	 * Service method that retrieves the whole information about a notification -
	 * OpenSearch logs, legal fact metadata document, attached to the notification
	 * documents and payment documents
	 * @param requestData the input data of type {@link NotificationInfoRequestDto}
	 * @return {@link BaseResponseDto} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException in case of an IO error
	 */
	BaseResponseDto getNotificationInfoLogs(NotificationInfoRequestDto requestData, String xPagopaHelpdUid) throws IOException;
	
	BaseResponseDto getAnonymizedSessionLogs(SessionLogsRequestDto requestData, String xPagopaHelpdUid) throws IOException;
	
	BaseResponseDto getDeanonimizedSessionLogs(SessionLogsRequestDto requestData, String xPagopaHelpdUid) throws IOException, LogExtractorException;
}
