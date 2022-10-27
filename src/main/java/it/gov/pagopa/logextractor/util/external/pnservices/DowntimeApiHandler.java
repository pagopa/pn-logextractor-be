package it.gov.pagopa.logextractor.util.external.pnservices;

import it.gov.pagopa.logextractor.dto.response.DowntimeCurrentStatusResponseDto;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusUpdateEventRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    public DowntimeCurrentStatusResponseDto getFunctionalitiesStatus() throws LogExtractorException {
        DowntimeCurrentStatusResponseDto response = client.getForEntity(downtimeStatusURL,
                DowntimeCurrentStatusResponseDto.class).getBody();
        if(response == null) {
            throw new LogExtractorException("Functionality status response is null");
        }
        return response;
    }

    public void addStatusChangeEvent(List<PnStatusUpdateEventRequestDto> pnStatusUpdateEventRequestDto) {
        client.postForObject(downtimeEventsURL, pnStatusUpdateEventRequestDto, Void.class);
    }
}
