package it.gov.pagopa.logextractor.exception;

import java.io.IOException;
//import javax.validation.ConstraintViolation;
//import javax.validation.ConstraintViolationException;
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
import it.gov.pagopa.logextractor.dto.response.ApiError;
import it.gov.pagopa.logextractor.util.Constants;
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
        return ResponseEntity.internalServerError().body(new ApiError(Constants.GENERIC_INTERNAL_SERVER_ERROR));
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
        return ResponseEntity.internalServerError().body(new ApiError(Constants.GENERIC_INTERNAL_SERVER_ERROR));
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
        return ResponseEntity.internalServerError().body(new ApiError(Constants.GENERIC_INTERNAL_SERVER_ERROR));
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
        return ResponseEntity.internalServerError().body(new ApiError(Constants.GENERIC_INTERNAL_SERVER_ERROR));
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
        return ResponseEntity.internalServerError().body(new ApiError(Constants.GENERIC_INTERNAL_SERVER_ERROR));
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
		return ResponseEntity.internalServerError().body(new ApiError(Constants.GENERIC_INTERNAL_SERVER_ERROR));
	}
	
	/**
	 * Manages the {@link InterruptedException} creating a new {@link ResponseEntity} and sending it to the client
	 * with error code 500 and a custom error message
	 * @param ex The intercepted exception
	 * @return A new {@link ResponseEntity} with {@link ApiError} body
	 * */
	@ExceptionHandler(InterruptedException.class)
    protected ResponseEntity<ApiError> handleInterruptedException(InterruptedException ex) {
        log.error(ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.internalServerError().body(new ApiError(Constants.GENERIC_INTERNAL_SERVER_ERROR));
    }
	
	
	/*@ExceptionHandler({ ConstraintViolationException.class })
    protected ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        StringBuilder builder = new StringBuilder("");
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            builder.append(" " + violation.getMessage() + ",");
        }
        log.error("Constraint violation exception:\n" + builder);
        return ResponseEntity.badRequest().body(new ApiError("Informazioni non valide"));
    }*/
}