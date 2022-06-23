package it.gov.pagopa.logextractor.exception;

import java.io.IOException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.dto.response.ApiError;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	
	@ExceptionHandler(IOException.class)
    protected ResponseEntity<ApiError> handleIOException(IOException ex) {
        log.error("IO exception:\n" + ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.internalServerError().body(new ApiError("Errore nell'elaborazione della richiesta"));
    }
	
	@ExceptionHandler(CsvDataTypeMismatchException.class)
    protected ResponseEntity<ApiError> handleCsvDataTypeMismatchException(CsvDataTypeMismatchException ex) {
        log.error("CSV data type mismatch exception:\n" + ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.internalServerError().body(new ApiError("Errore nell'elaborazione della richiesta"));
    }
	
	@ExceptionHandler(CsvRequiredFieldEmptyException.class)
    protected ResponseEntity<ApiError> handleCsvRequiredFieldEmptyException(CsvRequiredFieldEmptyException ex) {
        log.error("CSV required field empty exception:\n" + ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.internalServerError().body(new ApiError("Errore nell'elaborazione della richiesta"));
    }
	
	@ExceptionHandler(HttpServerErrorException.class)
    protected ResponseEntity<ApiError> handleHttpServerErrorException(HttpServerErrorException ex) {
        log.error("HTTP server error exception:\n" + ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.internalServerError().body(new ApiError("Errore nell'elaborazione della richiesta"));
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
	
	@ExceptionHandler(HttpClientErrorException.class)
	protected ResponseEntity<ApiError> handleHttpServerErrorException(HttpClientErrorException ex) {
		log.error("HTTP client error exception:\n" + ExceptionUtils.getStackTrace(ex));
		return ResponseEntity.internalServerError().body(new ApiError("Errore nell'elaborazione della richiesta"));
	}
}