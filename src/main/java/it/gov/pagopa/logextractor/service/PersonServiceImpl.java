package it.gov.pagopa.logextractor.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.GetBasicDataResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonTaxIdRequestDto;
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
	public GetBasicDataResponseDto getTaxId(PersonTaxIdRequestDto requestData,
											String xPagopaHelpdUid,
											String xPagopaCxType) throws HttpServerErrorException, LogExtractorException {
		log.info("Tax id retrieve process - START - user={}, userType={}, internalId={}",
				xPagopaHelpdUid, xPagopaCxType, requestData.getPersonId());
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting tax id...");
		GetBasicDataResponseDto response = handler.getTaxCodeForPerson(requestData.getPersonId());
		long performanceMillis = System.currentTimeMillis() - serviceStartTime;
		log.info("Tax id retrieved in {} ms", performanceMillis);
		log.info("Tax id retrieve process - END in {} ms", performanceMillis);
		return response;
	}

	@Override
	public GetBasicDataResponseDto getPersonId(PersonPersonIdRequestDto requestData,
											   String xPagopaHelpdUid,
											   String xPagopaCxType) throws HttpServerErrorException, LogExtractorException {
		
		if (StringUtils.isNotBlank(requestData.getPiva())){
			requestData.setTaxId(requestData.getPiva());
		}
		log.info("Internal id retrieve process - START - user={}, userType={}, ticket number={}, " +
						"recipientType={}, taxId={}", xPagopaHelpdUid, xPagopaCxType, requestData.getTicketNumber(),
				requestData.getRecipientType(), "***********");
		long serviceStartTime = System.currentTimeMillis();
		log.info("Getting internal id...");
		String response =  handler.getUniqueIdentifierForPerson(requestData.getRecipientType(), requestData.getTaxId().toUpperCase());
		log.info("Service response: internalId={}", response);
		long performanceMillis = System.currentTimeMillis() - serviceStartTime;
		log.info("Internal id retrieved in {} ms", performanceMillis);
		log.info("Internal id retrieve process - END in {} ms", performanceMillis);
		GetBasicDataResponseDto serviceResponse = new GetBasicDataResponseDto();
		serviceResponse.setData(response);
		serviceResponse.setMessage(ResponseConstants.SUCCESS_RESPONSE_MESSAGE);
		return serviceResponse;
	}	
}