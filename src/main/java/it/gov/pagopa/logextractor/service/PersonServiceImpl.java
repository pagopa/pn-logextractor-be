package it.gov.pagopa.logextractor.service;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.ResponseConstants;
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
	public GetBasicDataResponseDto getTaxId(String personId) throws HttpServerErrorException, LogExtractorException {
		log.info("Tax id retrieve process - START - user={} - internalId={}", MDC.get("user_identifier"), personId);
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting tax id...");
		GetBasicDataResponseDto response = handler.getTaxCodeForPerson(personId);
		long performanceMillis = System.currentTimeMillis() - serviceStartTime;
		log.info("Tax id retrieved in {} ms", performanceMillis);
		log.info("Tax id retrieve process - END in {} ms", performanceMillis + Long.parseLong(MDC.get("validationTime")));
		return response;
	}

	@Override
	public GetBasicDataResponseDto getPersonId(RecipientTypes recipientType, String ticketNumber, String taxId) throws HttpServerErrorException, LogExtractorException {
		log.info("Internal id retrieve process - START - user={}, ticket number={}, recipientType={}, taxId={}", 
				MDC.get("user_identifier"), ticketNumber, recipientType, taxId);
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting internal id...");
		String response =  handler.getUniqueIdentifierForPerson(recipientType, taxId);
		log.info("Service response: internalId={}", response);
		long performanceMillis = System.currentTimeMillis() - serviceStartTime;
		log.info("Internal id retrieved in {} ms", performanceMillis);
		log.info("Internal id retrieve process - END in {} ms", performanceMillis + Long.parseLong(MDC.get("validationTime")));
		return GetBasicDataResponseDto.builder().data(response).message(ResponseConstants.SUCCESS_RESPONSE_MESSAGE).build();
	}	
}