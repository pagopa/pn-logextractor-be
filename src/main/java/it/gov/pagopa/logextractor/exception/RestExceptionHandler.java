package it.gov.pagopa.logextractor.exception;

import java.io.IOException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
//import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import it.gov.pagopa.logextractor.dto.response.ApiError;
//import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
//@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	
	@ExceptionHandler(LogExtractorException.class)
    protected ResponseEntity<ApiError> handleLogExtractorException(LogExtractorException ex) {
        //log.error("ERROR: Business Exception: " + ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.internalServerError().body(new ApiError(ex));
    }
	
	@ExceptionHandler(IOException.class)
    protected ResponseEntity<ApiError> handleIOException(IOException ex) {
        //log.error("ERROR: Business Exception: " + ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.internalServerError().body(new ApiError("Errore nell'elaborazione della richiesta"));
    }
	
	@ExceptionHandler(HttpServerErrorException.class)
    protected ResponseEntity<ApiError> handleHttpServerErrorException(HttpServerErrorException ex) {
        //log.error("ERROR: Business Exception: " + ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.internalServerError().body(new ApiError("Errore nell'elaborazione della richiesta"));
    }
	
	@ExceptionHandler({ ConstraintViolationException.class })
    protected ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        StringBuilder builder = new StringBuilder("");
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            builder.append(" " + violation.getMessage() + ",");
        }
        //log.error("ERROR: Constraint Violation: " + builder);
        return ResponseEntity.badRequest().body(new ApiError("Informazioni non valide"));
    }
}