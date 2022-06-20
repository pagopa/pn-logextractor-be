package it.gov.pagopa.logextractor;

import java.nio.charset.Charset;
import java.time.LocalDate;

import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.logextractor.dto.request.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonTaxIdRequestDto;
import it.gov.pagopa.logextractor.dto.response.EnsureRecipientByExternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;

public class AbstractMock {
	
	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));	
	protected final String identifierUrl = "/logextractor/v1/persons/person-id";
	protected final String taxCodeUrl = "/logextractor/v1/persons/tax-id";
	protected final String personUrl ="/logextractor/v1/logs/persons";
	private static ObjectMapper mapper = new ObjectMapper();
	
	@SuppressWarnings("unchecked")
	protected void mockUniqueIdentifierForPerson(RestTemplate client) {
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class)))
				.thenReturn(EnsureRecipientByExternalIdResponseDto.builder().internalId("123").build());
	}

	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPerson(RestTemplate client) {
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class)))
				.thenReturn(GetRecipientDenominationByInternalIdResponseDto.builder().taxId("BRMRSS63A02A001D").build());
	}
	

	protected static String getMockPersonLogsRequestDto() throws JsonProcessingException {
		PersonLogsRequestDto dto = new PersonLogsRequestDto();
		dto.setDateFrom(LocalDate.now().toString());
		dto.setDateTo(LocalDate.now().toString());
		dto.setDeanonimization(true);
		dto.setIun("oMib-bnCM-sMyR-629600-T-0");
		dto.setPersonId("123");
		dto.setTaxId("BRMRSS63A02A001D");
		dto.setTicketNumber("123");
		return mapper.writeValueAsString(dto);
	}
	
	protected static String getMockPersonPersonIdRequestDto() throws JsonProcessingException {
		PersonPersonIdRequestDto dto = new PersonPersonIdRequestDto();
		dto.setRecipientType("PF");
		dto.setTaxId("BRMRSS63A02A001D");
		dto.setTicketNumber("123");
		return mapper.writeValueAsString(dto);
	}
	
	protected static String getMockPersonTaxIdRequestDto() throws JsonProcessingException {
		PersonTaxIdRequestDto dto = new PersonTaxIdRequestDto();
		dto.setPersonId("123");
		return mapper.writeValueAsString(dto);
	}

}
