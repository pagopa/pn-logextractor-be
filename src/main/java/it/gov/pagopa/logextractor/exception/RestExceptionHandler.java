package it.gov.pagopa.logextractor.exception;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import it.gov.pagopa.logextractor.pn_logextractor_be.model.Problem;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.ProblemError;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception handling class
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Manages the {@link LogExtractorException} creating a new {@link ResponseEntity} and sending it
   * to the client with error code 500 and information about the error
   *
   * @param ex The intercepted exception
   * @return A new {@link ResponseEntity} with {@link Problem} body
   */
  @ExceptionHandler(LogExtractorException.class)
  protected ResponseEntity<Problem> handleLogExtractorException(LogExtractorException ex) {
    log.error(ExceptionUtils.getStackTrace(ex));
    Problem problemResponse = createProblem(HttpStatus.INTERNAL_SERVER_ERROR,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_ENGLISH_MESSAGE,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_MESSAGE);
    return ResponseEntity.internalServerError().body(problemResponse);
  }

  /**
   * Manages the {@link MethodArgumentNotValidException} creating a new {@link ResponseEntity} and
   * sending it to the client with error code 400 and information about the error
   *
   * @param ex The intercepted exception
   * @return A new {@link ResponseEntity} with {@link Problem} body
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatus status, WebRequest request) {
    log.error(ExceptionUtils.getStackTrace(ex));
    Problem problemResponse = createProblem(HttpStatus.BAD_REQUEST,
        ResponseConstants.GENERIC_BAD_REQUEST_ERROR_ENGLISH_MESSAGE,
        ResponseConstants.GENERIC_BAD_REQUEST_ERROR_MESSAGE);
    return ResponseEntity.internalServerError().body(problemResponse);
  }

  /**
   * Manages the {@link IOException} creating a new {@link ResponseEntity} and sending it to the
   * client with error code 500 and information about the error
   *
   * @param ex The intercepted exception
   * @return A new {@link ResponseEntity} with {@link Problem} body
   */
  @ExceptionHandler(IOException.class)
  protected ResponseEntity<Problem> handleIOException(IOException ex) {
    log.error(ExceptionUtils.getStackTrace(ex));
    Problem problemResponse = createProblem(HttpStatus.INTERNAL_SERVER_ERROR,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_ENGLISH_MESSAGE,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_MESSAGE);
    return ResponseEntity.internalServerError().body(problemResponse);
  }

  /**
   * Manages the {@link CsvDataTypeMismatchException} creating a new {@link ResponseEntity} and
   * sending it to the client with error code 500 and information about the error
   *
   * @param ex The intercepted exception
   * @return A new {@link ResponseEntity} with {@link Problem} body
   */
  @ExceptionHandler(CsvDataTypeMismatchException.class)
  protected ResponseEntity<Problem> handleCsvDataTypeMismatchException(
      CsvDataTypeMismatchException ex) {
    log.error(ExceptionUtils.getStackTrace(ex));
    Problem problemResponse = createProblem(HttpStatus.INTERNAL_SERVER_ERROR,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_ENGLISH_MESSAGE,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_MESSAGE);
    return ResponseEntity.internalServerError().body(problemResponse);
  }

  /**
   * Manages the {@link CsvRequiredFieldEmptyException} creating a new {@link ResponseEntity} and
   * sending it to the client with error code 500 and information about the error
   *
   * @param ex The intercepted exception
   * @return A new {@link ResponseEntity} with {@link Problem} body
   */
  @ExceptionHandler(CsvRequiredFieldEmptyException.class)
  protected ResponseEntity<Problem> handleCsvRequiredFieldEmptyException(
      CsvRequiredFieldEmptyException ex) {
    log.error(ExceptionUtils.getStackTrace(ex));
    Problem problemResponse = createProblem(HttpStatus.INTERNAL_SERVER_ERROR,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_ENGLISH_MESSAGE,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_MESSAGE);
    return ResponseEntity.internalServerError().body(problemResponse);
  }

  /**
   * Manages the {@link HttpServerErrorException} creating a new {@link ResponseEntity} and sending
   * it to the client with error code 500 and information about the error
   *
   * @param ex The intercepted exception
   * @return A new {@link ResponseEntity} with {@link Problem} body
   */
  @ExceptionHandler(HttpServerErrorException.class)
  protected ResponseEntity<Problem> handleHttpServerErrorException(HttpServerErrorException ex) {
    log.error(ExceptionUtils.getStackTrace(ex));
    Problem problemResponse = createProblem(HttpStatus.INTERNAL_SERVER_ERROR,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_ENGLISH_MESSAGE,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_MESSAGE);
    return ResponseEntity.internalServerError().body(problemResponse);
  }

  /**
   * Manages the {@link HttpClientErrorException} creating a new {@link ResponseEntity} and sending
   * it to the client with error code 500 and information about the error
   *
   * @param ex The intercepted exception
   * @return A new {@link ResponseEntity} with {@link Problem} body
   */
  @ExceptionHandler(HttpClientErrorException.class)
  protected ResponseEntity<Problem> handleHttpServerErrorException(HttpClientErrorException ex) {
    log.error(ExceptionUtils.getStackTrace(ex));

    Problem problemResponse = createProblem(HttpStatus.INTERNAL_SERVER_ERROR,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_ENGLISH_MESSAGE,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_MESSAGE);
    return ResponseEntity.internalServerError().body(problemResponse);
  }

  @ExceptionHandler(LogExtractorIntegrationException.class)
  protected ResponseEntity<Problem> handleLogExtractorIntegrationException(
      LogExtractorIntegrationException ex) {

    Problem problemResponse = createProblem(HttpStatus.INTERNAL_SERVER_ERROR,
        ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_ENGLISH_MESSAGE, ex.getMessage());
    return ResponseEntity.internalServerError().body(problemResponse);

  }

  private Problem createProblem(HttpStatus responseCode, String titleMessage,
      String detailMessage) {
    Problem genericError = new Problem();
    genericError.setStatus(responseCode.value());
    genericError.setTitle(titleMessage);
    genericError.setDetail(detailMessage);
    genericError.setTraceId(MDC.get(LoggingConstants.TRACE_ID_PLACEHOLDER));
    genericError.setTimestamp(OffsetDateTime.now());
    ProblemError errorDetails = new ProblemError();
    errorDetails.setCode(responseCode.toString());
    errorDetails.setDetail(detailMessage);
    List<ProblemError> errorDetailsList = new ArrayList<>();
    errorDetailsList.add(errorDetails);
    genericError.setErrors(errorDetailsList);
    return genericError;
  }
}
