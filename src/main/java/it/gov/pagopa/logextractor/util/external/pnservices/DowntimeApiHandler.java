package it.gov.pagopa.logextractor.util.external.pnservices;

import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusUpdateEventRequestDto;
import it.gov.pagopa.logextractor.util.constant.CognitoConstants;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DowntimeApiHandler {

    @Autowired
    @Qualifier("simpleRestTemplate")
    RestTemplate client;

    @Value("${external.downtime.status.url}")
    String downtimeStatusURL;

    @Value("${external.downtime.events.url}")
    String downtimeEventsURL;

    public PnStatusResponseDto getFunctionalitiesStatus() throws LogExtractorException {
        PnStatusResponseDto response = client.getForEntity(downtimeStatusURL, PnStatusResponseDto.class).getBody();
        if(response == null) {
            throw new LogExtractorException("Functionality status response is null");
        }
        return response;
    }

    public void addStatusChangeEvent(List<PnStatusUpdateEventRequestDto> pnStatusUpdateEventRequestDto) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_PDF);
        requestHeaders.set("x-pagopa-pn-uid", MDC.get(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER));
        List<MediaType> acceptedTypes = new ArrayList<>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HttpEntity<?> entity = new HttpEntity<>(pnStatusUpdateEventRequestDto.toString(), requestHeaders);
        client.postForEntity(downtimeEventsURL, entity, Void.class);
    }
}
