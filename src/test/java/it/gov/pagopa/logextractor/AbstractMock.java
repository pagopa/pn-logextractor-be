package it.gov.pagopa.logextractor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.HashMap;

import org.junit.Before;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.dto.request.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.dto.request.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonTaxIdRequestDto;
import it.gov.pagopa.logextractor.dto.request.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.dto.response.EnsureRecipientByExternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
//import it.gov.pagopa.logextractor.dto.response.LegalFactDownloadMetadataResponseDto;
//import it.gov.pagopa.logextractor.dto.response.NotificationAttachmentDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationsGeneralDataResponseDto;
import it.gov.pagopa.logextractor.dto.response.PublicAuthorityMappingResponseDTO;
import it.gov.pagopa.logextractor.util.RecipientTypes;

public abstract class AbstractMock {	

	@Autowired MockMvc mvc;
	@MockBean
	@Qualifier("simpleRestTemplate") RestTemplate client;	
	@MockBean
	@Qualifier("openSearchRestTemplate") RestTemplate openClient;
	@Value("classpath:data/notification.json")	private Resource mockNotification;
	@Value("classpath:data/notification_general_data.json")	private Resource mockNotificationGeneralData;
	@Value("classpath:data/notification_general_data2.json") private Resource mockNotificationGeneralData2;
	@Value("classpath:data/authresponse.json") private Resource authResponse;
	@Value("classpath:data/pasummarieslist.json") private Resource paSummariesList;
	@Value("classpath:data/recipient_internal.json") private Resource mockRecipentInternal;
	

	
	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));	
	protected final String identifierUrl = "/logextractor/v1/persons/person-id";
	protected final String taxCodeUrl = "/logextractor/v1/persons/tax-id";
	protected final String personUrl ="/logextractor/v1/logs/persons";
	protected final String notificationUrl = "/logextractor/v1/logs/notifications/monthly";
	protected final String notificationInfoUrl = "/logextractor/v1/logs/notifications/info";
	protected final String processesUrl = "/logextractor/v1/logs/processes";
	protected final String fakeHeader = "Basic YWxhZGRpbjpvcGVuc2VzYW1l";
	private static ObjectMapper mapper = new ObjectMapper();
	

	@SuppressWarnings("unchecked")
	protected void mockUniqueIdentifierForPerson() throws RestClientException, IOException {
		//The first return is used to simulate authentication
		Mockito.when(client.postForObject(Mockito.anyString(),Mockito.any(), Mockito.any(Class.class)))
				.thenReturn(getStringFromResourse(authResponse), mapper.writeValueAsString(EnsureRecipientByExternalIdResponseDto.builder().internalId("123").build()));
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
	
	@SuppressWarnings("unchecked")
	protected void mockPersonsLogResponse() throws IOException {
		//String jsonResponse = getStringFromResourse(mockNotification);
		NotificationDetailsResponseDto jsonResponse = getNotificationFromResource(mockNotification);
		ResponseEntity<Object> response = new ResponseEntity<Object>(jsonResponse, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any())).thenReturn(response);
		String jsonDocSearch = "{\"responses\":[{\"hits\":{\"hits\":[{\"_source\":{\"_source\":\"3242342323\"}}]}}]}";
		ResponseEntity<String> responseSearch = new ResponseEntity<String>(jsonDocSearch, HttpStatus.OK);
		Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(responseSearch);
		//every argument of thenReturn is a different type of rest call
//		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(
//				mockLegalFactDownloadMetadataResponseDto(), 
//				"test".getBytes(),
//				mockNotificationAttachmentDownloadMetadataResponseDto(),
//				"test".getBytes(),
//				mockNotificationAttachmentDownloadMetadataResponseDto(),
//				"test".getBytes(),
//				mockNotificationAttachmentDownloadMetadataResponseDto(),
//				"test".getBytes());
		mockUniqueIdentifierForPerson();
	}
	
	@SuppressWarnings("unchecked")
	protected void mockPersonsLogUseCase6Response() throws IOException {
		//String jsonResponse = getStringFromResourse(mockNotification);
		NotificationDetailsResponseDto jsonResponse = getNotificationFromResource(mockNotification);
		String mock = getStringFromResourse(paSummariesList);
		ResponseEntity<Object> response2 = new ResponseEntity<Object>(mock, HttpStatus.OK);
		ResponseEntity<Object> response = new ResponseEntity<Object>(jsonResponse, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any())).thenReturn(response2, response);
		String jsonDocSearch = "{\"responses\":[{\"hits\":{\"hits\":[{\"_source\":{\"_source\":\"3242342323\"}}]}}]}";
		ResponseEntity<String> responseSearch = new ResponseEntity<String>(jsonDocSearch, HttpStatus.OK);
		Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(responseSearch);
		mockUniqueIdentifierForPerson();
	}
	
	
	@SuppressWarnings("unchecked")
	protected void mockPublicAuthorityIdAndNotificationsBetweenMonths(RestTemplate client) throws StreamReadException, DatabindException, IOException {
		PublicAuthorityMappingResponseDTO getPublicAuthorityMappingResponseDTO = new PublicAuthorityMappingResponseDTO();
		PublicAuthorityMappingResponseDTO[] array = new PublicAuthorityMappingResponseDTO[1];
		getPublicAuthorityMappingResponseDTO.setId("123");
		getPublicAuthorityMappingResponseDTO.setName("");
		array[0] = getPublicAuthorityMappingResponseDTO;
		NotificationsGeneralDataResponseDto jsonResponse = getNotificationGeneralDataFromResource(mockNotificationGeneralData);
        ResponseEntity<NotificationsGeneralDataResponseDto> responseRecipient1 = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        ResponseEntity<PublicAuthorityMappingResponseDTO[]> responseRecipient = new ResponseEntity<>(array, HttpStatus.OK);
        Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class), ArgumentMatchers.anyMap()))
                .thenReturn(responseRecipient, responseRecipient1);
    }
	
	
	@SuppressWarnings("unchecked")
	protected void mockNotificationResponse() throws IOException {
		//String mock2 = getStringFromResourse(mockNotificationGeneralData2);
		NotificationsGeneralDataResponseDto mock2 = getNotificationGeneralDataFromResource(mockNotificationGeneralData2);
		ResponseEntity<Object> response2 = new ResponseEntity<Object>(mock2, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class))).thenReturn(response2);	
	}
	
	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPerson(RestTemplate client) throws StreamReadException, DatabindException, IOException {
		GetRecipientDenominationByInternalIdResponseDto[] array = new GetRecipientDenominationByInternalIdResponseDto[1];
		GetRecipientDenominationByInternalIdResponseDto jsonResponse = getInternalFromResource(mockRecipentInternal);
		array[0] = jsonResponse;
        ResponseEntity<GetRecipientDenominationByInternalIdResponseDto[]> responseRecipient = new ResponseEntity<>(array, HttpStatus.OK);
        Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class), ArgumentMatchers.anyMap()))
                .thenReturn(responseRecipient);
    }
	
//	protected LegalFactDownloadMetadataResponseDto mockLegalFactDownloadMetadataResponseDto() {
//	    LegalFactDownloadMetadataResponseDto dto = new LegalFactDownloadMetadataResponseDto();
//        dto.setContentLength(0);
//        dto.setFilename("mockito.test");
//        dto.setRetryAfter(1);
//        dto.setUrl("http://test.it");
//        return dto;	
//	}
	
//	protected NotificationAttachmentDownloadMetadataResponseDto mockNotificationAttachmentDownloadMetadataResponseDto() {
//		NotificationAttachmentDownloadMetadataResponseDto dto = new NotificationAttachmentDownloadMetadataResponseDto();
//		dto.setContentLength(0);
//		dto.setContentType("json");
//        dto.setFilename("mockito.test");
//        dto.setRetryAfter(1);
//        dto.setUrl("http://test.it");
//        dto.setSha256("");
//        return dto;
//	}

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
		//dto.setIpaCode("123");
		dto.setReferenceMonth("2022-06");
		dto.setTicketNumber("345");
		dto.setPublicAuthorityName("abc");
		dto.setEndMonth("2022-07");
		return mapper.writeValueAsString(dto);
	}
	
	protected static String getMockNotificationsRequestDto() throws JsonProcessingException {
		NotificationInfoRequestDto dto = new NotificationInfoRequestDto();
		dto.setTicketNumber("345");
		dto.setIun("ABCHFGJRENDLAPEORIFKDNSME");
		return mapper.writeValueAsString(dto);
	}	
	
	private static String getStringFromResourse(Resource resource) throws IOException {
		return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
	}
	
	private static NotificationDetailsResponseDto getNotificationFromResource(Resource resource) throws StreamReadException, DatabindException, IOException {
		return mapper.readValue(resource.getInputStream(), NotificationDetailsResponseDto.class);
	}
	
	private static NotificationsGeneralDataResponseDto getNotificationGeneralDataFromResource(Resource resource) throws StreamReadException, DatabindException, IOException {
		return mapper.readValue(resource.getInputStream(), NotificationsGeneralDataResponseDto.class);
	}
	
	private static GetRecipientDenominationByInternalIdResponseDto getInternalFromResource(Resource resource) throws StreamReadException, DatabindException, IOException {
		return mapper.readValue(resource.getInputStream(), GetRecipientDenominationByInternalIdResponseDto.class);
	}
}
