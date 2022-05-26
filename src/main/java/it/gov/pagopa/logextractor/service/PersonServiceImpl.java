package it.gov.pagopa.logextractor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import it.gov.pagopa.logextractor.dto.response.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.deanonimizationservice.DeanonimizationApiHandler;

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
	public GetBasicDataResponseDto getPersonsBasicData(RecipientTypes recipientType, String ticketNumber, String taxId,
			String personId) throws HttpServerErrorException {

		DeanonimizationApiHandler handler = new DeanonimizationApiHandler();
		GetBasicDataResponseDto basicData = null;

		if (ticketNumber != null && taxId != null && personId == null) {
			basicData = handler.getUniqueIdentifierForPerson(recipientType, taxId, getUniqueIdURL);
		} else {
			if (personId != null && ticketNumber == null && taxId == null) {
				basicData = handler.getTaxCodeForPerson(personId, getTaxCodeURL);
			}
		}
		return basicData;
	}
}