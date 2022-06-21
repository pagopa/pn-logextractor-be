package it.gov.pagopa.logextractor;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.HashMap;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.logextractor.dto.request.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonTaxIdRequestDto;
import it.gov.pagopa.logextractor.dto.response.EnsureRecipientByExternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.util.RecipientTypes;

public abstract class AbstractMock {	

	@Autowired MockMvc mvc;
	@MockBean
	@Qualifier("simpleRestTemplate") RestTemplate client;	
	@MockBean
	@Qualifier("openSearchRestTemplate") RestTemplate openClient;
	
	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));	
	protected final String identifierUrl = "/logextractor/v1/persons/person-id";
	protected final String taxCodeUrl = "/logextractor/v1/persons/tax-id";
	protected final String personUrl ="/logextractor/v1/logs/persons";
	protected final String notificationUrl = "/logextractor/v1/logs/notifications/monthly";
	private static ObjectMapper mapper = new ObjectMapper();
	
	@SuppressWarnings("unchecked")
	protected void mockUniqueIdentifierForPerson(RestTemplate client) {
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class)))
				.thenReturn(EnsureRecipientByExternalIdResponseDto.builder().internalId("123").build());
	}

	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPerson200(RestTemplate client) {
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class)))
				.thenReturn(GetRecipientDenominationByInternalIdResponseDto.builder().taxId("BRMRSS63A02A001D").build());
	}
	
	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPersonServerError(RestTemplate client, HttpStatus errorStatus) {
    	HttpServerErrorException errorResponse = new HttpServerErrorException(errorStatus, "", "".getBytes(), Charset.defaultCharset());	   	
    	Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenThrow(errorResponse);
	}
	
	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPersonClientError(RestTemplate client, HttpStatus errorStatus) {
    	HttpClientErrorException errorResponse = new HttpClientErrorException(errorStatus, "", "".getBytes(), Charset.defaultCharset());	   	
    	Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenThrow(errorResponse);
	}
	
	protected void mockPersonsLogResponse(RestTemplate client, RestTemplate client2) {
		String jsonResponse= "{\"timeline\":[{\"category\":\"REQUEST_ACCEPTED\",\"timestamp\":\"2007-12-03T10:15:30+01:00\"}]}";
		ResponseEntity<Object> response = new ResponseEntity<Object>(jsonResponse, HttpStatus.OK);	
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any())).thenReturn(response);	
		String jsonDocSearch= "{\"responses\":[{\"hits\":{\"hits\":[{\"_source\":{\"_source\":\"3242342323\"}}]}}]}";
		ResponseEntity<String> responseSearch = new ResponseEntity<String>(jsonDocSearch, HttpStatus.OK);
        Mockito.when(client2.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.<Class<String>>any())
        ).thenReturn(responseSearch);

        mockUniqueIdentifierForPerson(client);
		
	}
	
	@SuppressWarnings("unchecked")
	protected void mockNotificationResponse(RestTemplate client) {
		String mock = "{\"resultsPage\":[{\"recipients\":[{\"recipients\":{\"iun\":\"ABC\",\"sentAt\":\"123\",\"subject\":\"test\"}}],\"iun\":\"ABC\",\"sentAt\":\"123\",\"subject\":\"test\"}]}";
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);	
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class))).thenReturn(response);	
		
	}

	protected static String getMockPersonLogsRequestDto(int useCase, boolean isDeanonimization) throws JsonProcessingException {
		PersonLogsRequestDto dto = new PersonLogsRequestDto();
		dto.setDateFrom(LocalDate.now().toString());
		dto.setDateTo(LocalDate.now().toString());
		dto.setDeanonimization(isDeanonimization);
		dto.setPersonId("123");
		dto.setTaxId("BRMRSS63A02A001D");
		dto.setTicketNumber("123");
		switch (useCase) {
			case 3:
			case 7:
				dto.setIun(null);
				dto.setRecipientType(RecipientTypes.PG);
				break;
			case 4:
			case 8:
				dto.setIun("oMib-bnCM-sMyR-629600-T-0");
				break;
			default:
				break;
		}
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
	
	protected static String getMockMonthlyNotificationsRequestDto() throws JsonProcessingException {
		MonthlyNotificationsRequestDto dto = new MonthlyNotificationsRequestDto();
		dto.setIpaCode("123");
		dto.setReferenceMonth("2022-06");
		dto.setTicketNumber("345");
		return mapper.writeValueAsString(dto);
	}
}
