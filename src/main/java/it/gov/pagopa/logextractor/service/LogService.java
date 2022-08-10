package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.text.ParseException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.dto.response.BaseResponseDTO;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.RecipientTypes;

public interface LogService {
	
	/**
	 * Service method that retrieves the anonymized logs related to a person's activities history in a period
	 * or to a notification's activities history within 3 months from its legal start date
	 * 
	 * @param dateFrom the period start date
	 * @param dateTo the period end date
	 * @param ticketNumber the ticket number the user is working on
	 * @param iun the notification identifier
	 * @param personId the person anonymized identifier
	 * @return {@link BaseResponseDTO} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException
	 */
	BaseResponseDTO getAnonymizedPersonLogs(String dateFrom, String dateTo, String ticketNumber, String iun, String personId) throws IOException, LogExtractorException;
	
	/**
	 * Service method that retrieves informations about the notifications sent by a public authority in a specific month 
	 * 
	 * @param ticketNumber the ticket number the user is working on
	 * @param referenceMonth the reference month for the notifications' extraction
	 * @param ipaCode the public authority identifier
	 * @return {@link BaseResponseDTO} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException
	 * @throws ParseException
	 * @throws CsvDataTypeMismatchException
	 * @throws CsvRequiredFieldEmptyException
	 */
	BaseResponseDTO getMonthlyNotifications(String ticketNumber, String referenceMonth, String endMonth, String publicAuthorityName) throws IOException, ParseException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, LogExtractorException;
	
	/**
	 * Service method that retrieves the anonymized logs belonging to the same process within a period
	 * 
	 * @param dateFrom the period start date
	 * @param dateTo the period end date
	 * @param traceId the process identifier
	 * @return {@link BaseResponseDTO} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException
	 */
	BaseResponseDTO getTraceIdLogs(String dateFrom, String dateTo, String traceId) throws IOException;
	
	/**
	 * Service method that retrieves the de-anonymized logs related to a person's activities history in a period
	 * or to a notification's activities history within 3 months from its legal start date
	 * 
	 * @param recipientType the person type, only values from {@link RecipientTypes} list allowed
	 * @param dateFrom the period start date
	 * @param dateTo the period end date
	 * @param ticketNumber the ticket number the user is working on
	 * @param taxid the person identifier
	 * @param iun the notification identifier
	 * @return {@link BaseResponseDTO} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException
	 */	
	BaseResponseDTO getDeanonymizedPersonLogs(RecipientTypes recipientType, String dateFrom, String dateTo, String ticketNumber, String taxid,String iun) throws IOException, LogExtractorException;
	
	/**
	 * Service method that retrieves the whole information about a notification -
	 * OpenSearch logs, legal fact metadata document, attached to the notification
	 * documents and payment documents
	 * 
	 * @param ticketNumber the ticket number the user is working on
	 * @param iun the notification IUN
	 * @return {@link BaseResponseDTO} containing a byte array
	 *         representation of the output zip archive and the password to access
	 *         its files
	 * @throws IOException
	 * @throws InterruptedException
	 */
	BaseResponseDTO getNotificationInfoLogs(String ticketNumber, String iun) throws IOException, InterruptedException;
}
