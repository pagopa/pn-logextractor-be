package it.gov.pagopa.logextractor.service;

import java.util.List;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusUpdateEventRequestDto;
import it.gov.pagopa.logextractor.util.constant.HeaderConstants;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import it.gov.pagopa.logextractor.util.external.pnservices.DowntimeApiHandler;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DowntimeServiceImpl implements DowntimeService{

    @Autowired
    DowntimeApiHandler downtimeApiHandler;

    @Override
    public BaseResponseDto addStatusChangeEvent(List<PnStatusUpdateEventRequestDto> pnStatusUpdateEventRequestDto, String xPagopaHelpdUid) {
        log.info("Status change event addition process - START - user={}",
        		xPagopaHelpdUid);
        long serviceStartTime = System.currentTimeMillis();
        log.info("Adding a new status change event...");
        downtimeApiHandler.addStatusChangeEvent(pnStatusUpdateEventRequestDto, xPagopaHelpdUid);
        log.info("Status change event added in {} ms", System.currentTimeMillis() - serviceStartTime);
        BaseResponseDto response = new BaseResponseDto();
        response.setMessage(ResponseConstants.SUCCESS_RESPONSE_MESSAGE);
        log.info("Status change event addition process - END in {} ms",
                (System.currentTimeMillis() - serviceStartTime));
        return response;
    }

    @Override
    public PnStatusResponseDto getCurrentStatus(String xPagopaHelpdUid) throws LogExtractorException {
        log.info("PN functionalities status retrieve process - START - user={} ",
        		xPagopaHelpdUid);
        long serviceStartTime = System.currentTimeMillis();
        log.info("Getting functionalities status...");
        PnStatusResponseDto currentStatusResponse = downtimeApiHandler.getFunctionalitiesStatus();
        log.info("Functionalities status retrieved in {} ms", System.currentTimeMillis() - serviceStartTime);
        log.info("PN functionalities status retrieve process - END in {} ms",
                (System.currentTimeMillis() - serviceStartTime));
        return currentStatusResponse;
    }
}
