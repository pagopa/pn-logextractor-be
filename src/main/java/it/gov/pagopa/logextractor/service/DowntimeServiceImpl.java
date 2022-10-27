package it.gov.pagopa.logextractor.service;

import it.gov.pagopa.logextractor.dto.response.DowntimeCurrentStatusResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetDowntimeStatusResponseDto;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusUpdateEventRequestDto;
import it.gov.pagopa.logextractor.util.constant.CognitoConstants;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import it.gov.pagopa.logextractor.util.external.pnservices.DowntimeApiHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DowntimeServiceImpl implements DowntimeService{

    @Autowired
    DowntimeApiHandler downtimeApiHandler;

    @Override
    public BaseResponseDto addStatusChangeEvent(List<PnStatusUpdateEventRequestDto> pnStatusUpdateEventRequestDto) {
        log.info("Status change event addition process - START - user={}",
                MDC.get(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER));
        long serviceStartTime = System.currentTimeMillis();
        log.info("Adding a new status change event...");
        downtimeApiHandler.addStatusChangeEvent(pnStatusUpdateEventRequestDto);
        log.info("Status change event added in {} ms", System.currentTimeMillis() - serviceStartTime);
        BaseResponseDto response = new BaseResponseDto();
        response.setMessage(ResponseConstants.SUCCESS_RESPONSE_MESSAGE);
        log.info("Status change event addition process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
        return response;
    }

    @Override
    public BaseResponseDto getCurrentStatus() throws LogExtractorException {
        log.info("PN functionalities status retrieve process - START - user={} ",
                MDC.get(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER));
        long serviceStartTime = System.currentTimeMillis();
        log.info("Getting functionalities status...");
        DowntimeCurrentStatusResponseDto currentStatus = downtimeApiHandler.getFunctionalitiesStatus();
        log.info("Functionalities status retrieved in {} ms", System.currentTimeMillis() - serviceStartTime);
        GetDowntimeStatusResponseDto response = new GetDowntimeStatusResponseDto();
        response.setMessage(ResponseConstants.SUCCESS_RESPONSE_MESSAGE);
        response.setFunctionalities(currentStatus.getFunctionalities());
        response.setOpenIncidents(currentStatus.getOpenIncidents());
        log.info("PN functionalities status retrieve process - END in {} ms", System.currentTimeMillis() - serviceStartTime);
        return response;
    }
}
