package it.gov.pagopa.logextractor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.HashMap;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.logextractor.dto.request.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.dto.request.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonTaxIdRequestDto;
import it.gov.pagopa.logextractor.dto.request.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.dto.response.EnsureRecipientByExternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.FileDownloadMetadataResponseDTO;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationHistoryResponseDTO;
import it.gov.pagopa.logextractor.dto.response.NotificationsGeneralDataResponseDto;
import it.gov.pagopa.logextractor.dto.response.PublicAuthorityMappingResponseDTO;
import it.gov.pagopa.logextractor.dto.response.SelfCarePaDataResponseDto;
import it.gov.pagopa.logextractor.util.RecipientTypes;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;


public abstract class AbstractMock {	

	@Autowired MockMvc mvc;
	
	@Mock
	NotificationApiHandler notificationApiHandler;
	
	@MockBean
	@Qualifier("simpleRestTemplate") RestTemplate client;	
	@MockBean
	@Qualifier("openSearchRestTemplate") RestTemplate openClient;
	
	@Value("classpath:data/notification.json")	private Resource mockNotification;
	@Value("classpath:data/notification_general_data.json")	private Resource mockNotificationGeneralData;
	@Value("classpath:data/notification_details_response.json")	private Resource mockNotificationDetails;
	@Value("classpath:data/notification_history_response.json")	private Resource mockNotificationHistory;
	@Value("classpath:data/notification_general_data2.json") private Resource mockNotificationGeneralData2;
	@Value("classpath:data/authresponse.json") private Resource authResponse;
	@Value("classpath:data/pasummarieslist.json") private Resource paSummariesList;
	@Value("classpath:data/recipient_internal.json") private Resource mockRecipentInternal;
	@Value("classpath:data/recipient_internal2.json") private Resource mockRecipentInternalTaxIdNull;
	@Value("classpath:data/metadata.json") private Resource mockFileKey;
	@Value("classpath:data/activated_id.json") private Resource mockSelfCarePaData;
	
	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));	
	protected final String identifierUrl = "/logextractor/v1/persons/person-id";
	protected final String taxCodeUrl = "/logextractor/v1/persons/tax-id";
	protected final String personUrl ="/logextractor/v1/logs/persons";
	protected final String notificationUrl = "/logextractor/v1/logs/notifications/monthly";
	protected final String notificationInfoUrl = "/logextractor/v1/logs/notifications/info";
	protected final String processesUrl = "/logextractor/v1/logs/processes";
	protected final String fakeHeader = "Basic YWxhZGRpbjpvcGVuc2VzYW1l";
	private static ObjectMapper mapper = new ObjectMapper();
	protected static String jsonDocSearchPF = "{\"responses\":[{\"hits\":{\"hits\":[{\"_source\":{\"_source\":\"3242342323\", \"cx_id\":\"PF-2dfc9690-a648-4462-986d-769d90752e6f\"}}]}}]}";
	protected static String jsonDocSearchPA = "{\"responses\":[{\"hits\":{\"hits\":[{\"_source\":{\"_source\":\"3242342323\", \"cx_id\":\"PA-2dfc9690-a648-4462-986d-769d90752e6f\"}}]}}]}";

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
	
	protected void mockPersonsLogResponse(String jsonDocSearch) throws IOException {
		NotificationDetailsResponseDto jsonResponse = getNotificationFromResource(mockNotification);
		ResponseEntity<Object> response = new ResponseEntity<Object>(jsonResponse, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any())).thenReturn(response);
		ResponseEntity<String> responseSearch = new ResponseEntity<String>(jsonDocSearch, HttpStatus.OK);
		Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(responseSearch);
		mockUniqueIdentifierForPerson();
	}
	
	@SuppressWarnings("unchecked")
	protected void mockPersonsLogUseCase6Response() throws IOException {
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
	protected void mockPublicAuthorityIdAndNotificationsBetweenMonths() throws StreamReadException, DatabindException, IOException {
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
	protected void mockDocumentsByMultiSearchQuery() throws StreamReadException, DatabindException, IOException {
		String jsonResponse = "";
        ResponseEntity<String> responseRecipient = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class)))
                .thenReturn(responseRecipient);
    }
	
	@SuppressWarnings("unchecked")
	protected void mockNotificationResponse() throws IOException {
		NotificationsGeneralDataResponseDto mock2 = getNotificationGeneralDataFromResource(mockNotificationGeneralData2);
		ResponseEntity<Object> response2 = new ResponseEntity<Object>(mock2, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class))).thenReturn(response2);	
	}
	
	@SuppressWarnings("unchecked")
	protected void mockNotificationDetailsResponse() throws IOException {
		NotificationDetailsResponseDto mock = getNotificationFromResource(mockNotificationDetails);
		ResponseEntity<Object> response2 = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class))).thenReturn(response2);	
	}
	
	protected void mockPublicAuthorityName() throws IOException {
		SelfCarePaDataResponseDto mock = getSelfCarePaDataResponseFromResource(mockSelfCarePaData);
		ResponseEntity<Object> response2 = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any())).thenReturn(response2);	
	}
	
	protected void mockNotificationHistoryResponse() throws StreamReadException, DatabindException, IOException {
		NotificationHistoryResponseDTO jsonResponse = getNotificationHistoryFromResource(mockNotificationHistory);
        ResponseEntity<NotificationHistoryResponseDTO> responseRecipientHistory = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<NotificationHistoryResponseDTO>>any(), ArgumentMatchers.anyMap()))
                .thenReturn(responseRecipientHistory);
    }
	
	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPerson() throws StreamReadException, DatabindException, IOException {
		GetRecipientDenominationByInternalIdResponseDto[] arrayJsonResponse = getRecipientInternalFromResource(mockRecipentInternal);
        ResponseEntity<GetRecipientDenominationByInternalIdResponseDto[]> responseRecipient = new ResponseEntity<>(arrayJsonResponse, HttpStatus.OK);
        Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class), ArgumentMatchers.anyMap()))
                .thenReturn(responseRecipient);
    }

	protected void mockFileDownloadMetadataResponseDTO() throws StreamReadException, DatabindException, IOException {
		FileDownloadMetadataResponseDTO jsonResponse = getFileDownloadMetadataFromResource(mockFileKey);
		
        ResponseEntity<FileDownloadMetadataResponseDTO> responseRecipient = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
        		ArgumentMatchers.any(HttpEntity.class),  ArgumentMatchers.<Class<FileDownloadMetadataResponseDTO>>any()))
                .thenReturn(responseRecipient);
    }
	
	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPerson_TaxIdNull() throws StreamReadException, DatabindException, IOException {
		GetRecipientDenominationByInternalIdResponseDto[] arrayJsonResponse = getRecipientInternalFromResource(mockRecipentInternalTaxIdNull);
        ResponseEntity<GetRecipientDenominationByInternalIdResponseDto[]> responseRecipient = new ResponseEntity<>(arrayJsonResponse, HttpStatus.OK);
        Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class), ArgumentMatchers.anyMap()))
                .thenReturn(responseRecipient);
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
	
	private static GetRecipientDenominationByInternalIdResponseDto[] getRecipientInternalFromResource(Resource resource) throws StreamReadException, DatabindException, IOException {
		return mapper.readValue(resource.getInputStream(), GetRecipientDenominationByInternalIdResponseDto[].class);
	}
	
	private static NotificationHistoryResponseDTO getNotificationHistoryFromResource(Resource resource) throws StreamReadException, DatabindException, IOException {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper.readValue(resource.getInputStream(), NotificationHistoryResponseDTO.class);
	}
	
	private static FileDownloadMetadataResponseDTO getFileDownloadMetadataFromResource(Resource resource) throws StreamReadException, DatabindException, IOException {
		return mapper.readValue(resource.getInputStream(), FileDownloadMetadataResponseDTO.class);
	}
	
	private static SelfCarePaDataResponseDto getSelfCarePaDataResponseFromResource(Resource resource) throws StreamReadException, DatabindException, IOException {
		return mapper.readValue(resource.getInputStream(), SelfCarePaDataResponseDto.class);
	}
	
}
