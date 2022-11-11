package it.gov.pagopa.logextractor.rest;

import it.gov.pagopa.logextractor.pn_logextractor_be.api.DowntimeApi;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusUpdateEventRequestDto;
import it.gov.pagopa.logextractor.service.DowntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class DowntimeController implements DowntimeApi {

    @Autowired
    DowntimeService downtimeService;

    @Override
    public ResponseEntity<BaseResponseDto> addStatusChangeEvent(List<PnStatusUpdateEventRequestDto> pnStatusUpdateEventRequestDto) {
        return ResponseEntity.ok().body(downtimeService.addStatusChangeEvent(pnStatusUpdateEventRequestDto));
    }

    @Override
    public ResponseEntity<PnStatusResponseDto> currentStatus() throws Exception {
        return ResponseEntity.ok().body(downtimeService.getCurrentStatus());
    }
}
