package it.gov.pagopa.logextractor.exception;

import java.io.IOException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.ApiError;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception handling class
 * */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	
	/**
	 * Manages the {@link LogExtractorException} creating a new {@link ResponseEntity} and sending it to the client
	 * with error code 500 and a custom error message
	 * @param ex The intercepted exception
	 * @return A new {@link ResponseEntity} with {@link ApiError} body
	 * */
	@ExceptionHandler(LogExtractorException.class)
    protected ResponseEntity<ApiError> handleLogExtractorException(LogExtractorException ex) {
        log.error(ExceptionUtils.getStackTrace(ex));
		ApiError errorResponse = new ApiError();
		errorResponse.setMessage(ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError().body(errorResponse);
    }
	
	/**
	 * Manages the {@link IOException} creating a new {@link ResponseEntity} and sending it to the client
	 * with error code 500 and a custom error message
	 * @param ex The intercepted exception
	 * @return A new {@link ResponseEntity} with {@link ApiError} body
	 * */
	@ExceptionHandler(IOException.class)
    protected ResponseEntity<ApiError> handleIOException(IOException ex) {
        log.error(ExceptionUtils.getStackTrace(ex));
		ApiError errorResponse = new ApiError();
		errorResponse.setMessage(ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR);
		return ResponseEntity.internalServerError().body(errorResponse);
    }
	
	/**
	 * Manages the {@link CsvDataTypeMismatchException} creating a new {@link ResponseEntity} and sending it to the client
	 * with error code 500 and a custom error message
	 * @param ex The intercepted exception
	 * @return A new {@link ResponseEntity} with {@link ApiError} body
	 * */
	@ExceptionHandler(CsvDataTypeMismatchException.class)
    protected ResponseEntity<ApiError> handleCsvDataTypeMismatchException(CsvDataTypeMismatchException ex) {
        log.error(ExceptionUtils.getStackTrace(ex));
		ApiError errorResponse = new ApiError();
		errorResponse.setMessage(ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR);
		return ResponseEntity.internalServerError().body(errorResponse);
    }
	
	/**
	 * Manages the {@link CsvRequiredFieldEmptyException} creating a new {@link ResponseEntity} and sending it to the client
	 * with error code 500 and a custom error message
	 * @param ex The intercepted exception
	 * @return A new {@link ResponseEntity} with {@link ApiError} body
	 * */
	@ExceptionHandler(CsvRequiredFieldEmptyException.class)
    protected ResponseEntity<ApiError> handleCsvRequiredFieldEmptyException(CsvRequiredFieldEmptyException ex) {
        log.error(ExceptionUtils.getStackTrace(ex));
		ApiError errorResponse = new ApiError();
		errorResponse.setMessage(ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR);
		return ResponseEntity.internalServerError().body(errorResponse);
    }
	
	/**
	 * Manages the {@link HttpServerErrorException} creating a new {@link ResponseEntity} and sending it to the client
	 * with error code 500 and a custom error message
	 * @param ex The intercepted exception
	 * @return A new {@link ResponseEntity} with {@link ApiError} body
	 * */
	@ExceptionHandler(HttpServerErrorException.class)
    protected ResponseEntity<ApiError> handleHttpServerErrorException(HttpServerErrorException ex) {
        log.error(ExceptionUtils.getStackTrace(ex));
		ApiError errorResponse = new ApiError();
		errorResponse.setMessage(ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR);
		return ResponseEntity.internalServerError().body(errorResponse);
    }
	
	/**
	 * Manages the {@link HttpClientErrorException} creating a new {@link ResponseEntity} and sending it to the client
	 * with error code 500 and a custom error message
	 * @param ex The intercepted exception
	 * @return A new {@link ResponseEntity} with {@link ApiError} body
	 * */
	@ExceptionHandler(HttpClientErrorException.class)
	protected ResponseEntity<ApiError> handleHttpServerErrorException(HttpClientErrorException ex) {
		log.error(ExceptionUtils.getStackTrace(ex));
		ApiError errorResponse = new ApiError();
		errorResponse.setMessage(ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR);
		return ResponseEntity.internalServerError().body(errorResponse);
	}
}