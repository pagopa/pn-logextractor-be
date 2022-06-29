package it.gov.pagopa.logextractor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.HashMap;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.logextractor.dto.request.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.dto.request.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonTaxIdRequestDto;
import it.gov.pagopa.logextractor.dto.request.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.dto.response.EnsureRecipientByExternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.LegalFactDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationAttachmentDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.util.RecipientTypes;

public abstract class AbstractMock {	

	@Autowired MockMvc mvc;
	@MockBean
	@Qualifier("simpleRestTemplate") RestTemplate client;	
	@MockBean
	@Qualifier("openSearchRestTemplate") RestTemplate openClient;
	@Value("classpath:data/notification.json")
	private Resource mockNotification;
	@Value("classpath:data/notification_general_data.json")
	private Resource mockNotificationGeneralData;
	@Value("classpath:data/notification_general_data2.json")
	private Resource mockNotificationGeneralData2;
	
	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));	
	protected final String identifierUrl = "/logextractor/v1/persons/person-id";
	protected final String taxCodeUrl = "/logextractor/v1/persons/tax-id";
	protected final String personUrl ="/logextractor/v1/logs/persons";
	protected final String notificationUrl = "/logextractor/v1/logs/notifications/monthly";
	protected final String notificationInfoUrl = "/logextractor/v1/logs/notifications/info";
	protected final String processesUrl = "/logextractor/v1/logs/processes";
	protected final String fakeHeader = "Basic YWxhZGRpbjpvcGVuc2VzYW1l";
	protected final String authResponse = "{\"UserAttributes\":[{\"Name\":\"custom:log_identifier\",\"Value\":\"BRMRSS63A02A001D\"}]}";
	private static ObjectMapper mapper = new ObjectMapper();
	

	@SuppressWarnings("unchecked")
	protected void mockUniqueIdentifierForPerson() {
		//The first return is used to simulate authentication
		Mockito.when(client.postForObject(Mockito.anyString(),Mockito.any(), Mockito.any(Class.class)))
				.thenReturn(authResponse, EnsureRecipientByExternalIdResponseDto.builder().internalId("123").build());
	}

	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPerson200() {
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class)))
				.thenReturn(GetRecipientDenominationByInternalIdResponseDto.builder().taxId("BRMRSS63A02A001D").build());
	}
	
	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPersonServerError(HttpStatus errorStatus) {
    	HttpServerErrorException errorResponse = new HttpServerErrorException(errorStatus, "", "".getBytes(), Charset.defaultCharset());	   	
    	Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenThrow(errorResponse);
	}
	
	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPersonClientError(HttpStatus errorStatus) {
    	HttpClientErrorException errorResponse = new HttpClientErrorException(errorStatus, "", "".getBytes(), Charset.defaultCharset());	   	
    	Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenThrow(errorResponse);
	}
	
	protected void mockPersonsLogResponse() throws IOException {
		String jsonResponse = StreamUtils.copyToString(mockNotification.getInputStream(), Charset.defaultCharset());
		ResponseEntity<Object> response = new ResponseEntity<Object>(jsonResponse, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any())).thenReturn(response);
		String jsonDocSearch = "{\"responses\":[{\"hits\":{\"hits\":[{\"_source\":{\"_source\":\"3242342323\"}}]}}]}";
		ResponseEntity<String> responseSearch = new ResponseEntity<String>(jsonDocSearch, HttpStatus.OK);
		Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(responseSearch);
		//every argument of thenReturn is a different type of rest call
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(
				mockLegalFactDownloadMetadataResponseDto(), 
				"test".getBytes(),
				mockNotificationAttachmentDownloadMetadataResponseDto(),
				"test".getBytes(),
				mockNotificationAttachmentDownloadMetadataResponseDto(),
				"test".getBytes(),
				mockNotificationAttachmentDownloadMetadataResponseDto(),
				"test".getBytes());
		mockUniqueIdentifierForPerson();

	}
	
	@SuppressWarnings("unchecked")
	protected void mockNotificationResponse() throws IOException {
		String mock = StreamUtils.copyToString(mockNotificationGeneralData.getInputStream(), Charset.defaultCharset());
		String mock2 = StreamUtils.copyToString(mockNotificationGeneralData2.getInputStream(), Charset.defaultCharset());
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		ResponseEntity<Object> response2 = new ResponseEntity<Object>(mock2, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class))).thenReturn(response2);	
	}
	
	protected LegalFactDownloadMetadataResponseDto mockLegalFactDownloadMetadataResponseDto() {
	    LegalFactDownloadMetadataResponseDto dto = new LegalFactDownloadMetadataResponseDto();
        dto.setContentLength(0);
        dto.setFilename("mockito.test");
        dto.setRetryAfter(1);
        dto.setUrl("http://test.it");
        return dto;	
	}
	
	protected NotificationAttachmentDownloadMetadataResponseDto mockNotificationAttachmentDownloadMetadataResponseDto() {
		NotificationAttachmentDownloadMetadataResponseDto dto = new NotificationAttachmentDownloadMetadataResponseDto();
		dto.setContentLength(0);
		dto.setContentType("json");
        dto.setFilename("mockito.test");
        dto.setRetryAfter(1);
        dto.setUrl("http://test.it");
        dto.setSha256("");
        return dto;
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
	
	protected static String getMockTraceIdLogsRequestDto(String dateFrom, String dateTo, String ticketNumber, String traceId) throws JsonProcessingException {
		TraceIdLogsRequestDto dto = new TraceIdLogsRequestDto();
		dto.setDateFrom(dateFrom);
		dto.setDateTo(dateTo);
		dto.setTicketNumber(ticketNumber);
		dto.setTraceId(traceId);
		return mapper.writeValueAsString(dto);
	}
	
	protected static String getMockPersonPersonIdRequestDto() throws JsonProcessingException {
		PersonPersonIdRequestDto dto = new PersonPersonIdRequestDto();
		dto.setRecipientType("PF");
		dto.setTicketNumber("123");
		dto.setTaxId("BRMRSS63A02A001D");
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
	
	
	protected static String getMockNotificationsRequestDto() throws JsonProcessingException {
		NotificationInfoRequestDto dto = new NotificationInfoRequestDto();
		dto.setTicketNumber("345");
		dto.setIun("ABCHFGJRENDLAPEORIFKDNSME");
		return mapper.writeValueAsString(dto);
	}
}
