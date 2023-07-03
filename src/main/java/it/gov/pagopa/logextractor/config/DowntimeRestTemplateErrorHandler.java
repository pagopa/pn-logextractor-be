package it.gov.pagopa.logextractor.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StreamUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.logextractor.exception.LogExtractorIntegrationException;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import it.gov.pagopa.logextractor.util.constant.ValidationConstants;

@Component
@Slf4j
public class DowntimeRestTemplateErrorHandler implements ResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    return (response.getStatusCode().is4xxClientError() || response.getStatusCode()
        .is5xxServerError());
  }

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
    log.error("Exception occurred when adding new downtime event:\n{}", responseBody);
    if (response.getStatusCode().is5xxServerError()) {
      throw new LogExtractorIntegrationException(
          ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_MESSAGE);
    } else if (response.getStatusCode().is4xxClientError()) {
      if (response.getStatusCode() == HttpStatus.CONFLICT) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String detail = jsonNode.get("detail").asText();
        Pattern pattern = Pattern.compile(ValidationConstants.DOWNTIME_RESPONSE_DATE_PATTERN);
        Matcher matcher = pattern.matcher(detail);

        String startDate = "";
        String endDate = "";

        if (matcher.find()) {
          startDate = matcher.group(1);
        }
        detail = StringUtils.substringAfter(detail, startDate);
        matcher = pattern.matcher(detail);
        if (matcher.find()) {
          endDate = matcher.group(1);
        }

        throw new LogExtractorIntegrationException(
            "Evento downtime già presente nel periodo compreso tra il giorno " + OffsetDateTime.parse(
                startDate).format(DateTimeFormatter.ofPattern(
                "dd/MM/yyyy 'alle' HH:mm")) + " ed il giorno " + OffsetDateTime.parse(endDate)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm")));
      } else {
        throw new LogExtractorIntegrationException(
            ResponseConstants.GENERIC_INTERNAL_SERVER_ERROR_MESSAGE);
      }
    }
  }
}
