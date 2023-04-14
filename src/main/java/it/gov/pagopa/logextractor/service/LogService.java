package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.*;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationDownloadFileData;

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
	void getAnonymizedPersonLogs(PersonLogsRequestDto requestData,
											String xPagopaHelpdUid,
											String xPagopaCxType) throws IOException;
	
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
	void getMonthlyNotifications(MonthlyNotificationsRequestDto requestData,
											String xPagopaHelpdUid,
											String xPagopaCxType) throws IOException, ParseException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, LogExtractorException;
	
	/**
	 * Service method that retrieves the anonymized logs belonging to the same process within a period
	 * @param requestData the input data of type {@link TraceIdLogsRequestDto}
	 * @return {@link BaseResponseDto} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException in case of an IO error
	 * @throws LogExtractorException in case of a business logic error
	 */
	void getTraceIdLogs(TraceIdLogsRequestDto requestData,
								   String xPagopaHelpdUid,
								   String xPagopaCxType) throws IOException, LogExtractorException;
	
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
	void getDeanonimizedPersonLogs(PersonLogsRequestDto requestData,
											  String xPagopaHelpdUid,
											  String xPagopaCxType) throws IOException, LogExtractorException;
	
	/**
	 * Service method that retrieves the whole information about a notification -
	 * OpenSearch logs, legal fact metadata document, attached to the notification
	 * documents and payment documents
	 * @param requestData the input data of type {@link NotificationInfoRequestDto}
	 * @return List<NotificationDownloadFileData>
	 * @throws IOException in case of an IO error
	 */
	List<NotificationDownloadFileData> getNotificationInfoLogs(NotificationInfoRequestDto requestData,
											String xPagopaHelpdUid, String xPagopaCxType) throws IOException;
	
	void getAnonymizedSessionLogs(SessionLogsRequestDto requestData,
											 String xPagopaHelpdUid, String xPagopaCxType) throws IOException;
	
	void getDeanonimizedSessionLogs(SessionLogsRequestDto requestData,
											   String xPagopaHelpdUid,
											   String xPagopaCxType) throws IOException, LogExtractorException;
}
