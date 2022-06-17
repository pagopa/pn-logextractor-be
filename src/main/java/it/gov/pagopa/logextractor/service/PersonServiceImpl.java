package it.gov.pagopa.logextractor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;

/**
 * Implementation class of {@link PersonService}
 */
@Service
public class PersonServiceImpl implements PersonService {

	@Value("${external.denomination.ensureRecipientByExternalId.url}")
	String getUniqueIdURL;

	@Value("${external.denomination.getRecipientDenominationByInternalId.url}")
	String getTaxCodeURL;

	@Override
	public GetBasicDataResponseDto getTaxId(String personId) throws HttpServerErrorException {
		return new DeanonimizationApiHandler().getTaxCodeForPerson(personId, getTaxCodeURL);
	}

	@Override
	public GetBasicDataResponseDto getPersonId(RecipientTypes recipientType, String ticketNumber, String taxId) throws HttpServerErrorException {
		return new DeanonimizationApiHandler().getUniqueIdentifierForPerson(recipientType, taxId, getUniqueIdURL);
	}	
}