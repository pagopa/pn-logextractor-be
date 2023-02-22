package it.gov.pagopa.logextractor.util.external.pnservices;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusUpdateEventRequestDto;

/**
 * Uility class for integrations with Piattaforma Notifiche downtime service
 **/
@Component
public class DowntimeApiHandler {

    @Autowired
    @Qualifier("simpleRestTemplate")
    RestTemplate client;

    @Value("${external.downtime.status.url}")
    String downtimeStatusURL;

    @Value("${external.downtime.events.url}")
    String downtimeEventsURL;

    /**
     * Performs a GET HTTP request to downtime microservice to obtain the current PN functionalities' status
     * @return The list of down functionalities and the related inefficiencies' data or an empty list if every
     * functionality is up
     * @throws LogExtractorException if the external service response is null
     * */
    public PnStatusResponseDto getFunctionalitiesStatus() throws LogExtractorException {
        PnStatusResponseDto response = client.getForEntity(downtimeStatusURL, PnStatusResponseDto.class).getBody();
        if(response == null) {
            throw new LogExtractorException("Functionality status response is null");
        }
        return response;
    }

    /**
     * Performs a POST HTTP request to downtime microservice to save a new record of down or up for a PN functionality
     * */
    public void addStatusChangeEvent(List<PnStatusUpdateEventRequestDto> pnStatusUpdateEventRequestDto, String xPagopaHelpdUid) {
    	for(PnStatusUpdateEventRequestDto pnStatusUpdate : pnStatusUpdateEventRequestDto) {
    		pnStatusUpdate.setSource(xPagopaHelpdUid);
    	}
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("x-pagopa-pn-uid", xPagopaHelpdUid);
        List<MediaType> acceptedTypes = new ArrayList<>();
        acceptedTypes.add(MediaType.APPLICATION_PROBLEM_JSON);
        requestHeaders.setAccept(acceptedTypes);
        HttpEntity<?> entity = new HttpEntity<>(pnStatusUpdateEventRequestDto, requestHeaders);
        client.postForEntity(downtimeEventsURL, entity, Void.class);
    }
}
