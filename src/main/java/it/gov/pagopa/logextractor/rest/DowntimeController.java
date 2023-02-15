package it.gov.pagopa.logextractor.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.pn_logextractor_be.api.DowntimeApi;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusUpdateEventRequestDto;
import it.gov.pagopa.logextractor.service.DowntimeService;

@RestController
public class DowntimeController implements DowntimeApi {

	@Autowired
	DowntimeService downtimeService;

	@Override
    public ResponseEntity<BaseResponseDto> addStatusChangeEvent(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
    		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType,
    		@RequestBody List<PnStatusUpdateEventRequestDto> pnStatusUpdateEventRequestDto) {
        return ResponseEntity.ok().body(downtimeService.addStatusChangeEvent(pnStatusUpdateEventRequestDto, xPagopaUid));
    }

	@Override
	public ResponseEntity<PnStatusResponseDto> currentStatus(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType) throws Exception {
		return ResponseEntity.ok().body(downtimeService.getCurrentStatus(xPagopaUid));
	}
}
