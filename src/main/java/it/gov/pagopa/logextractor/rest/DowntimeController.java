package it.gov.pagopa.logextractor.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<BaseResponseDto> addStatusChangeEvent(String xPagopaPnUid, String xPagopaPnCxType, List<PnStatusUpdateEventRequestDto> pnStatusUpdateEventRequestDto) {
		return ResponseEntity.ok().body(downtimeService.addStatusChangeEvent(pnStatusUpdateEventRequestDto,
				xPagopaPnUid, xPagopaPnCxType));
	}

	@Override
	public ResponseEntity<PnStatusResponseDto> currentStatus(String xPagopaPnUid, String xPagopaPnCxType) throws Exception {
		return ResponseEntity.ok().body(downtimeService.getCurrentStatus(xPagopaPnUid, xPagopaPnCxType));
	}
}
