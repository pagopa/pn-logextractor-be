package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.scheduling.annotation.Async;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.SessionLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.TraceIdLogsRequestDto;

public interface LogService {
	
	/**
	 * Service method that retrieves the anonymized logs related to a person's activities history in a period
	 * or to a notification's activities history within 3 months from its legal start date
	 * @param requestData the input data of type {@link PersonLogsRequestDto}
	 * @return 
	 * @throws IOException in case of an IO error
	 */
	@Async
	String getAnonymizedPersonLogs(String key, String pass, PersonLogsRequestDto requestData,
											String xPagopaHelpdUid,
											String xPagopaCxType) throws IOException;
	
	/**
	 * Service method that retrieves informations about the notifications sent by a public authority in a specific month
	 * @param requestData the input data of type {@link MonthlyNotificationsRequestDto}
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
	 * @return 
	 * @throws IOException in case of an IO error
	 * @throws LogExtractorException in case of a business logic error
	 */
	@Async
	void getDeanonimizedPersonLogs(String key, String zipPassword, PersonLogsRequestDto requestData,
											  String xPagopaHelpdUid,
											  String xPagopaCxType) throws IOException, LogExtractorException;
	
	/**
	 * Service method that retrieves the whole information about a notification -
	 * OpenSearch logs, legal fact metadata document, attached to the notification
	 * documents and payment documents
	 * @param requestData the input data of type {@link NotificationInfoRequestDto}
	 * @throws IOException in case of an IO error
	 */
	@Async
	void getNotificationInfoLogs(String key, String zipPassword,NotificationInfoRequestDto requestData,
											String xPagopaHelpdUid, String xPagopaCxType) throws IOException;
	
	void getAnonymizedSessionLogs(SessionLogsRequestDto requestData,
											 String xPagopaHelpdUid, String xPagopaCxType) throws IOException;
	
	void getDeanonimizedSessionLogs(SessionLogsRequestDto requestData,
											   String xPagopaHelpdUid,
											   String xPagopaCxType) throws IOException, LogExtractorException;
}
