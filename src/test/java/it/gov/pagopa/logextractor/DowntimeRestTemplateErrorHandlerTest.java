package it.gov.pagopa.logextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.logextractor.config.DowntimeRestTemplateErrorHandler;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.Problem;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.ProblemError;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

@SpringBootTest
class DowntimeRestTemplateErrorHandlerTest {

  ClientHttpResponse response;
  ProblemError problemError;
  Problem problem;

  @Autowired
  ObjectMapper simpleObjectMapper;

  @Autowired
  DowntimeRestTemplateErrorHandler downtimeRestTemplateErrorHandler;

  @BeforeEach
  void setup() {
    problemError = new ProblemError();
    problem = new Problem();
    problemError.setDetail(
        "Downtime already open, startDate(GMT/UTC) " + "{} = 2023-04-10T14:06:47.327Z, endDate(GMT/UTC) {} = 2023-04-10T17:06:47.327Z");
    problemError.setCode("409 CONFLICT");
    problem.setStatus(409);
    problem.setDetail(
        "Downtime already open, startDate(GMT/UTC) " + "{} = 2023-04-10T14:06:47.327Z, endDate(GMT/UTC) {} = 2023-04-10T17:06:47.327Z");
    problem.setTitle(
        "Conflict in request. Requested resource in conflict with the current state of the server.");
    problem.setErrors(List.of(problemError));
  }

  @Test
  @DisplayName("4xx error is intercepted")
  void testHasError_whenResponseIs4xx_thenReturnTrue() throws IOException {
    response = new ClientHttpResponse() {
      @Override
      public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.CONFLICT;
      }

      @Override
      public int getRawStatusCode() throws IOException {
        return 409;
      }

      @Override
      public String getStatusText() throws IOException {
        return null;
      }

      @Override
      public void close() {
      }

      @Override
      public InputStream getBody() throws IOException {
        return IOUtils.toInputStream(simpleObjectMapper.writeValueAsString(problem),
            Charset.defaultCharset());
      }

      @Override
      public HttpHeaders getHeaders() {
        return null;
      }
    };
    Assertions.assertTrue(downtimeRestTemplateErrorHandler.hasError(response));
  }

  @Test
  @DisplayName("5xx error is intercepted")
  void testHasError_whenResponseIs5xx_thenReturnTrue() throws IOException {
    response = new ClientHttpResponse() {
      @Override
      public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.INTERNAL_SERVER_ERROR;
      }

      @Override
      public int getRawStatusCode() throws IOException {
        return 500;
      }

      @Override
      public String getStatusText() throws IOException {
        return null;
      }

      @Override
      public void close() {
      }

      @Override
      public InputStream getBody() throws IOException {
        return IOUtils.toInputStream(simpleObjectMapper.writeValueAsString(problem),
            Charset.defaultCharset());
      }

      @Override
      public HttpHeaders getHeaders() {
        return null;
      }
    };
    Assertions.assertTrue(downtimeRestTemplateErrorHandler.hasError(response));
  }

  @Test
  @DisplayName("409 error response is intercepted and an integration exception is thrown")
  void testHandleError_whenResponseIs409_thenThrowsIntegrationException() throws IOException {
    response = new ClientHttpResponse() {
      @Override
      public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.CONFLICT;
      }

      @Override
      public int getRawStatusCode() throws IOException {
        return 409;
      }

      @Override
      public String getStatusText() throws IOException {
        return null;
      }

      @Override
      public void close() {
      }

      @Override
      public InputStream getBody() throws IOException {
        return IOUtils.toInputStream(simpleObjectMapper.writeValueAsString(problem),
            Charset.defaultCharset());
      }

      @Override
      public HttpHeaders getHeaders() {
        return null;
      }
    };
    Assertions.assertThrows(IOException.class,
        () -> downtimeRestTemplateErrorHandler.handleError(response));
  }

  @Test
  @DisplayName("500 error response is intercepted and an integration exception is thrown")
  void testHandleError_whenResponseIs500_thenThrowsIntegrationException() throws IOException {
    response = new ClientHttpResponse() {
      @Override
      public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.INTERNAL_SERVER_ERROR;
      }

      @Override
      public int getRawStatusCode() throws IOException {
        return 500;
      }

      @Override
      public String getStatusText() throws IOException {
        return null;
      }

      @Override
      public void close() {
      }

      @Override
      public InputStream getBody() throws IOException {
        return IOUtils.toInputStream(simpleObjectMapper.writeValueAsString(problem),
            Charset.defaultCharset());
      }

      @Override
      public HttpHeaders getHeaders() {
        return null;
      }
    };
    Assertions.assertThrows(IOException.class,
        () -> downtimeRestTemplateErrorHandler.handleError(response));
  }

  @Test
  @DisplayName("4xx error response is intercepted and an integration exception is thrown")
  void testHandleError_whenResponseIs4xx_thenThrowsIntegrationException() throws IOException {
    response = new ClientHttpResponse() {
      @Override
      public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.NOT_ACCEPTABLE;
      }

      @Override
      public int getRawStatusCode() throws IOException {
        return 406;
      }

      @Override
      public String getStatusText() throws IOException {
        return null;
      }

      @Override
      public void close() {
      }

      @Override
      public InputStream getBody() throws IOException {
        return IOUtils.toInputStream(simpleObjectMapper.writeValueAsString(problem),
            Charset.defaultCharset());
      }

      @Override
      public HttpHeaders getHeaders() {
        return null;
      }
    };
    Assertions.assertThrows(IOException.class,
        () -> downtimeRestTemplateErrorHandler.handleError(response));
  }
}
