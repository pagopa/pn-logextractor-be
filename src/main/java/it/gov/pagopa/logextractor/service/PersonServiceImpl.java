package it.gov.pagopa.logextractor.service;

import java.time.Instant;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation class of {@link PersonService}
 */
@Service
@Slf4j
public class PersonServiceImpl implements PersonService {

	@Value("${external.denomination.ensureRecipientByExternalId.url}")
	String getUniqueIdURL;

	@Value("${external.denomination.getRecipientDenominationByInternalId.url}")
	String getTaxCodeURL;
	
	@Autowired
	DeanonimizationApiHandler handler;

	@Override
	public GetBasicDataResponseDto getTaxId(String personId) throws HttpServerErrorException {
		log.info("Tax id retrieve process - START");
		long millis = Instant.now().getEpochSecond();
		log.info("Calling deanonimization service, internalId={}", personId);
		GetBasicDataResponseDto response = handler.getTaxCodeForPerson(personId, getTaxCodeURL);
		log.info("Returning deanonimized data: " + response);
		log.info("Tax id retrieve process - END in {} milliseconds", Instant.now().getEpochSecond() - millis);
		return response;
	}

	@Override
	public GetBasicDataResponseDto getPersonId(RecipientTypes recipientType, String ticketNumber, String taxId) throws HttpServerErrorException {
		log.info("Internal id retrieve process - START - user={}, ticket number={}", MDC.get("user_identifier"), ticketNumber);
		long millis = Instant.now().getEpochSecond();
		log.info("Calling deanonimization service, recipientType={}, taxId={}", recipientType, taxId);
		GetBasicDataResponseDto response =  handler.getUniqueIdentifierForPerson(recipientType, taxId, getUniqueIdURL);
		log.info("Returning deanonimized data: " + response);
		log.info("Internal id retrieve process - END in {} milliseconds", Instant.now().getEpochSecond() - millis);
		return response;
	}	
}