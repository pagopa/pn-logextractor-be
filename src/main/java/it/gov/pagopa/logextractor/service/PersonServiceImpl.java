package it.gov.pagopa.logextractor.service;

import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonTaxIdRequestDto;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.util.constant.CognitoConstants;
import it.gov.pagopa.logextractor.util.constant.LoggingConstants;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation class of {@link PersonService}
 */
@Service
@Slf4j
public class PersonServiceImpl implements PersonService {
	
	@Autowired
	DeanonimizationApiHandler handler;

	@Override
	public GetBasicDataResponseDto getTaxId(PersonTaxIdRequestDto requestData) throws HttpServerErrorException, LogExtractorException {
		log.info("Tax id retrieve process - START - user={} - internalId={}", MDC.get(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER), requestData.getPersonId());
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting tax id...");
		GetBasicDataResponseDto response = handler.getTaxCodeForPerson(requestData.getPersonId());
		long performanceMillis = System.currentTimeMillis() - serviceStartTime;
		log.info("Tax id retrieved in {} ms", performanceMillis);
		log.info("Tax id retrieve process - END in {} ms", performanceMillis + Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
		return response;
	}

	@Override
	public GetBasicDataResponseDto getPersonId(PersonPersonIdRequestDto requestData) throws HttpServerErrorException, LogExtractorException {
		log.info("Internal id retrieve process - START - user={}, ticket number={}, recipientType={}, taxId={}", 
				MDC.get(CognitoConstants.USER_IDENTIFIER_PLACEHOLDER), requestData.getTicketNumber(),
				requestData.getRecipientType(), requestData.getTaxId());
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting internal id...");
		String response =  handler.getUniqueIdentifierForPerson(requestData.getRecipientType(), requestData.getTaxId().toUpperCase());
		log.info("Service response: internalId={}", response);
		long performanceMillis = System.currentTimeMillis() - serviceStartTime;
		log.info("Internal id retrieved in {} ms", performanceMillis);
		log.info("Internal id retrieve process - END in {} ms", performanceMillis + Long.parseLong(MDC.get(LoggingConstants.VALIDATION_TIME)));
		GetBasicDataResponseDto serviceResponse = new GetBasicDataResponseDto();
		serviceResponse.setData(response);
		serviceResponse.setMessage(ResponseConstants.SUCCESS_RESPONSE_MESSAGE);
		return serviceResponse;
	}	
}