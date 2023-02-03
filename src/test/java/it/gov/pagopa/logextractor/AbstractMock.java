package it.gov.pagopa.logextractor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.dto.response.EnsureRecipientByExternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.FileDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.GetRecipientDenominationByInternalIdResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationHistoryResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationsGeneralDataResponseDto;
import it.gov.pagopa.logextractor.dto.response.PublicAuthorityMappingResponseDto;
import it.gov.pagopa.logextractor.dto.response.SelfCarePaDataResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonPersonIdRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonTaxIdRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnFunctionality;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnFunctionalityStatus;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusUpdateEventRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusUpdateEventRequestDto.SourceTypeEnum;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.SessionLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;

public abstract class AbstractMock {

	@Autowired
	MockMvc mvc;

	@Mock
	NotificationApiHandler notificationApiHandler;

	@MockBean
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	@MockBean
	@Qualifier("openSearchRestTemplate")
	RestTemplate openClient;

	@Value("classpath:data/notification.json")
	protected Resource mockNotification;
	@Value("classpath:data/notification_general_data.json")
	protected Resource mockNotificationGeneralData;
	@Value("classpath:data/notification_details_response.json")
	protected Resource mockNotificationDetails;
	@Value("classpath:data/notification_history_response.json")
	protected Resource mockNotificationHistory;
	@Value("classpath:data/notification_general_data2.json")
	protected Resource mockNotificationGeneralData2;
	@Value("classpath:data/notification_general_data_empty.json")
	protected Resource mockNotificationGeneralDataEmpty;
	@Value("classpath:data/authresponse.json")
	protected Resource authResponse;
	@Value("classpath:data/authResponseNull.json")
	private Resource authResponseNull;
	@Value("classpath:data/pasummarieslist.json")
	protected Resource paSummariesList;
	@Value("classpath:data/recipient_internal.json")
	protected Resource mockRecipentInternal;
	@Value("classpath:data/recipient_internal2.json")
	protected Resource mockRecipentInternalTaxIdNull;
	@Value("classpath:data/metadata.json")
	protected Resource mockFileKey;
	@Value("classpath:data/activated_id.json")
	protected Resource mockSelfCarePaData;

	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
	protected final String identifierUrl = "/persons/v1/person-id";
	protected final String taxCodeUrl = "/persons/v1/tax-id";
	protected final String personUrl = "/logs/v1/persons";
	protected final String sessionUrl = "/logs/v1/sessions";
	protected final String notificationUrl = "/logs/v1/notifications/monthly";
	protected final String notificationInfoUrl = "/logs/v1/notifications/info";
	protected final String processesUrl = "/logs/v1/processes";
	protected final String healthcheckUrl = "/status";

	protected final String statusUrl = "/downtime/v1/status";
	protected final String eventsUrl = "/downtime/v1/events";

	protected final String fakeHeader = "Basic YWxhZGRpbjpvcGVuc2VzYW1l";
	private static ObjectMapper mapper = new ObjectMapper();

	// protected static String jsonDocSearchPF =
	// "{\"responses\":[{\"hits\":{\"hits\":[{\"_source\":{\"_source\":\"3242342323\",
	// \"cx_id\":\"PF-2dfc9690-a648-4462-986d-769d90752e6f\"}}]}}]}";
	// protected static String jsonDocSearchPA =
	// "{\"responses\":[{\"hits\":{\"hits\":[{\"_source\":{\"_source\":\"3242342323\",
	// \"cx_id\":\"PA-2dfc9690-a648-4462-986d-769d90752e6f\"}}]}}]}";

	protected final String jsonDocSearchPF = "{\"_scroll_id\":\"test\",\"hits\" : {\"hits\" : [{\"_source\":{\"_source\":\"3242342323\",\"cx_id\":\"PF-2dfc9690-a648-4462-986d-769d90752e6f\"}}]}}";
	protected final String jsonEmptyDocSearchPF = "{\"_scroll_id\":\"test\",\"hits\" : {\"hits\" : []}}";
	protected final String jsonDocSearchPA = "{\"_scroll_id\":\"test\",\"hits\" : {\"hits\" : [{\"_source\":{\"_source\":\"3242342323\",\"cx_id\":\"PA-2dfc9690-a648-4462-986d-769d90752e6f\"}}]}}";

	protected final String scrollMockSearch = "{\"_scroll_id\":\"test\",\"hits\" : {\"hits\" : []}}";

	@SuppressWarnings("unchecked")
	protected void mockUniqueIdentifierForPerson() throws RestClientException, IOException {
		// The first return is used to simulate authentication
		Mockito.when(client.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any(Class.class))).thenReturn(
				getStringFromResourse(authResponse), mapper.registerModule(new JavaTimeModule()).writeValueAsString(
						EnsureRecipientByExternalIdResponseDto.builder().internalId("123").build()));
	}

	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPerson200() {
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(
				GetRecipientDenominationByInternalIdResponseDto.builder().taxId("BRMRSS63A02A001D").build());
	}

	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPersonServerError(HttpStatus errorStatus) {
		HttpServerErrorException errorResponse = new HttpServerErrorException(errorStatus, "", "".getBytes(),
				Charset.defaultCharset());
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenThrow(errorResponse);
	}

	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPersonClientError(HttpStatus errorStatus) {
		HttpClientErrorException errorResponse = new HttpClientErrorException(errorStatus, "", "".getBytes(),
				Charset.defaultCharset());
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenThrow(errorResponse);
	}

	@SuppressWarnings("unchecked")
	protected void mockMissingUniqueIdentifierForPerson() throws RestClientException, IOException {
		String userAttributes = getStringFromResourse(authResponseNull);
		// The first return is used to simulate authentication
		Mockito.when(client.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any(Class.class)))
				.thenReturn(userAttributes);
	}

	@SuppressWarnings("unchecked")
	protected void mockPersonsLogResponse(String jsonDocSearch) throws IOException {
		NotificationDetailsResponseDto jsonResponse = getNotificationFromResource(mockNotification);
		ResponseEntity<Object> response = new ResponseEntity<Object>(jsonResponse, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any())).thenReturn(response);
		ResponseEntity<String> responseSearch = new ResponseEntity<String>(jsonDocSearch, HttpStatus.OK);
		Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class), ArgumentMatchers.anyMap()))
				.thenReturn(responseSearch);
		ResponseEntity<String> scrollResponseSearch = new ResponseEntity<String>(scrollMockSearch, HttpStatus.OK);
		Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(scrollResponseSearch);
		mockUniqueIdentifierForPerson();
	}
	
	@SuppressWarnings("unchecked")
	protected void mockEmptyPersonsLogResponse(String jsonDocSearch) throws IOException {
		NotificationDetailsResponseDto jsonResponse = getNotificationFromResource(mockNotification);
		ResponseEntity<Object> response = new ResponseEntity<Object>(jsonResponse, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any())).thenReturn(response);
		ResponseEntity<String> responseSearch = new ResponseEntity<String>("", HttpStatus.OK);
		Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class), ArgumentMatchers.anyMap()))
				.thenReturn(responseSearch);
		ResponseEntity<String> scrollResponseSearch = new ResponseEntity<String>(scrollMockSearch, HttpStatus.OK);
		Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(scrollResponseSearch);
		mockUniqueIdentifierForPerson();
	}

	@SuppressWarnings("unchecked")
	protected void mockPersonsLogUseCase6Response() throws IOException {
		NotificationDetailsResponseDto jsonResponse = getNotificationFromResource(mockNotification);
		String mock = getStringFromResourse(paSummariesList);
		ResponseEntity<Object> response2 = new ResponseEntity<Object>(mock, HttpStatus.OK);
		ResponseEntity<Object> response = new ResponseEntity<Object>(jsonResponse, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any())).thenReturn(response2, response);
		String jsonDocSearch = "{\"_scroll_id\":\"test\",\"hits\" : {\"hits\" : [ ]}}";
		// String jsonDocSearch =
		// "{\"responses\":[{\"hits\":{\"hits\":[{\"_source\":{\"_source\":\"3242342323\"}}]}}]}";
		ResponseEntity<String> responseSearch = new ResponseEntity<String>(jsonDocSearch, HttpStatus.OK);
		Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(responseSearch);
		mockUniqueIdentifierForPerson();
	}

	@SuppressWarnings("unchecked")
	protected void mockPublicAuthorityIdAndNotificationsBetweenMonths(boolean isEmpty) throws IOException {
		PublicAuthorityMappingResponseDto getPublicAuthorityMappingResponseDTO = new PublicAuthorityMappingResponseDto();
		PublicAuthorityMappingResponseDto[] array = new PublicAuthorityMappingResponseDto[1];
		getPublicAuthorityMappingResponseDTO.setId("123");
		getPublicAuthorityMappingResponseDTO.setName("");
		array[0] = getPublicAuthorityMappingResponseDTO;
		NotificationsGeneralDataResponseDto jsonResponse = getNotificationGeneralDataFromResource(
				mockNotificationGeneralData);
		ResponseEntity<NotificationsGeneralDataResponseDto> responseRecipient1 = new ResponseEntity<>(jsonResponse,
				HttpStatus.OK);
		ResponseEntity<PublicAuthorityMappingResponseDto[]> responseRecipient = new ResponseEntity<>(array,
				HttpStatus.OK);
		if (!isEmpty) {
			Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
					ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class),
					ArgumentMatchers.anyMap())).thenReturn(responseRecipient, responseRecipient1);
		} else {
			NotificationsGeneralDataResponseDto jsonResponseEmpty = getNotificationGeneralDataFromResource(
					mockNotificationGeneralDataEmpty);
			ResponseEntity<NotificationsGeneralDataResponseDto> responseRecipientEmpty = new ResponseEntity<>(
					jsonResponseEmpty, HttpStatus.OK);
			Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
					ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class),
					ArgumentMatchers.anyMap())).thenReturn(responseRecipient, responseRecipientEmpty);
		}
	}

	@SuppressWarnings("unchecked")
	protected void mockDocumentsByMultiSearchQuery() throws IOException {
		String jsonResponse = "";
		ResponseEntity<String> responseRecipient = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
		Mockito.when(openClient.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class)))
				.thenReturn(responseRecipient);
	}

	@SuppressWarnings("unchecked")
	protected void mockNotificationResponse() throws IOException {
		NotificationsGeneralDataResponseDto mock2 = getNotificationGeneralDataFromResource(
				mockNotificationGeneralData2);
		ResponseEntity<Object> response2 = new ResponseEntity<Object>(mock2, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
				.thenReturn(response2);
	}

	protected void mockNotificationsIsEmpty() throws IOException {
		List<NotificationData> notifications = new ArrayList<>();
		Mockito.when(notificationApiHandler.getNotificationsByMonthsPeriod(Mockito.any(), Mockito.any(),
				Mockito.anyString())).thenReturn(notifications);
	}

	@SuppressWarnings("unchecked")
	protected void mockNotificationDetailsResponse() throws IOException {
		NotificationDetailsResponseDto mock = getNotificationFromResource(mockNotificationDetails);
		ResponseEntity<Object> response2 = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class)))
				.thenReturn(response2);
	}

	protected void mockPublicAuthorityName() throws IOException {
		SelfCarePaDataResponseDto mock = getSelfCarePaDataResponseFromResource(mockSelfCarePaData);
		ResponseEntity<Object> response2 = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForEntity(Mockito.anyString(), Mockito.any())).thenReturn(response2);
	}

	protected void mockNotificationHistoryResponse() throws IOException {
		NotificationHistoryResponseDto jsonResponse = getNotificationHistoryFromResource(mockNotificationHistory);
		ResponseEntity<NotificationHistoryResponseDto> responseRecipientHistory = new ResponseEntity<>(jsonResponse,
				HttpStatus.OK);
		Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<NotificationHistoryResponseDto>>any(),
				ArgumentMatchers.anyMap())).thenReturn(responseRecipientHistory);
	}

	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPerson() throws IOException {
		GetRecipientDenominationByInternalIdResponseDto[] arrayJsonResponse = getRecipientInternalFromResource(
				mockRecipentInternal);
		ResponseEntity<GetRecipientDenominationByInternalIdResponseDto[]> responseRecipient = new ResponseEntity<>(
				arrayJsonResponse, HttpStatus.OK);
		Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class), ArgumentMatchers.anyMap()))
				.thenReturn(responseRecipient);
	}

	protected void mockFileDownloadMetadataResponseDTO(Resource mockFileKeyJson) throws IOException {
		FileDownloadMetadataResponseDto jsonResponse = getFileDownloadMetadataFromResource(mockFileKeyJson);

		ResponseEntity<FileDownloadMetadataResponseDto> responseRecipient = new ResponseEntity<>(jsonResponse,
				HttpStatus.OK);
		Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<FileDownloadMetadataResponseDto>>any()))
				.thenReturn(responseRecipient);
	}

	@SuppressWarnings("unchecked")
	protected void mockTaxCodeForPerson_TaxIdNull() throws IOException {
		GetRecipientDenominationByInternalIdResponseDto[] arrayJsonResponse = getRecipientInternalFromResource(
				mockRecipentInternalTaxIdNull);
		ResponseEntity<GetRecipientDenominationByInternalIdResponseDto[]> responseRecipient = new ResponseEntity<>(
				arrayJsonResponse, HttpStatus.OK);
		Mockito.when(client.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.any(Class.class), ArgumentMatchers.anyMap()))
				.thenReturn(responseRecipient);
	}

	protected void getMockPnStatusResponseDto(PnStatusResponseDto fakePnStatusResponseDto)
			throws JsonProcessingException {
		ResponseEntity<PnStatusResponseDto> responseStatus = new ResponseEntity<>(fakePnStatusResponseDto,
				HttpStatus.OK);
		Mockito.when(
				client.getForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.<Class<PnStatusResponseDto>>any()))
				.thenReturn(responseStatus);
	}

	@SuppressWarnings("unchecked")
	protected void mockAddStatusChangeEvent(RestTemplate client) throws RestClientException, IOException {
		String mock = "";
		ResponseEntity<Object> response = new ResponseEntity<Object>(mock, HttpStatus.OK);
		Mockito.when(client.getForObject(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(response);
	}

	protected static String getMockPersonLogsRequestDto(int useCase, boolean isDeanonimization)
			throws JsonProcessingException {
		PersonLogsRequestDto dto = new PersonLogsRequestDto();
		dto.setDateFrom(LocalDate.now());
		dto.setDateTo(LocalDate.now());
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
		return mapper.registerModule(new JavaTimeModule()).writeValueAsString(dto);
	}

	protected static String getMockSessionLogsRequestDto(boolean isDeanonimization)	throws JsonProcessingException {
		SessionLogsRequestDto dto = new SessionLogsRequestDto();
		dto.setDateFrom(LocalDate.now());
		dto.setDateTo(LocalDate.now());
		dto.setDeanonimization(isDeanonimization);
		dto.setJti("12954F907C0535ABE97F761829C6BD11");
		dto.setTicketNumber("123");
		return mapper.registerModule(new JavaTimeModule()).writeValueAsString(dto);
	}
	
	protected static String getMockPersonLogsRequestDtoPersonIdNull() throws JsonProcessingException {
		PersonLogsRequestDto dto = new PersonLogsRequestDto();
		dto.setDateFrom(LocalDate.now());
		dto.setDateTo(LocalDate.now());
		dto.setDeanonimization(false);
		dto.setTaxId("BRMRSS63A02A001D");
		dto.setTicketNumber("123");
		dto.setIun(null);
		return mapper.registerModule(new JavaTimeModule()).writeValueAsString(dto);
	}

	protected static String getMockTraceIdLogsRequestDto(LocalDate dateFrom, LocalDate dateTo, String traceId)
			throws JsonProcessingException {
		TraceIdLogsRequestDto dto = new TraceIdLogsRequestDto();
		dto.setDateFrom(dateFrom);
		dto.setDateTo(dateTo);
		dto.setTraceId(traceId);
		return mapper.registerModule(new JavaTimeModule()).writeValueAsString(dto);
	}

	protected static String getMockPersonPersonIdRequestDto() throws JsonProcessingException {
		PersonPersonIdRequestDto dto = new PersonPersonIdRequestDto();
		dto.setRecipientType(RecipientTypes.PF);
		dto.setTicketNumber("123");
		dto.setTaxId("BRMRSS63A02A001D");
		return mapper.registerModule(new JavaTimeModule()).writeValueAsString(dto);
	}

	protected static String getMockPersonTaxIdRequestDto() throws JsonProcessingException {
		PersonTaxIdRequestDto dto = new PersonTaxIdRequestDto();
		dto.setPersonId("123");
		return mapper.registerModule(new JavaTimeModule()).writeValueAsString(dto);
	}

	protected static String getMockMonthlyNotificationsRequestDto() throws JsonProcessingException {
		MonthlyNotificationsRequestDto dto = new MonthlyNotificationsRequestDto();
		dto.setReferenceMonth(OffsetDateTime.now());
		dto.setTicketNumber("345");
		dto.setPublicAuthorityName("abc");
		dto.setEndMonth(OffsetDateTime.now());
		return mapper.registerModule(new JavaTimeModule()).writeValueAsString(dto);
	}

	protected static String getMockPnStatusUpdateEventRequestDto(PnFunctionalityStatus pnFunctionalityStatus)
			throws JsonProcessingException {
		List<PnStatusUpdateEventRequestDto> listPnStatusUpdateEventRequestDto = new ArrayList<>();
		List<PnFunctionality> listPnFunctionality = new ArrayList<>();
		PnStatusUpdateEventRequestDto pnStatusUpdateEventRequestDto = new PnStatusUpdateEventRequestDto();
		for (PnFunctionality pnFunctionality : PnFunctionality.values()) {
			listPnFunctionality.add(pnFunctionality);
		}
		pnStatusUpdateEventRequestDto.setTimestamp(OffsetDateTime.parse("2022-11-03T17:00:15.995Z"));
		pnStatusUpdateEventRequestDto.setStatus(pnFunctionalityStatus);
		pnStatusUpdateEventRequestDto.setSourceType(SourceTypeEnum.OPERATOR);
		pnStatusUpdateEventRequestDto.setFunctionality(listPnFunctionality);
		listPnStatusUpdateEventRequestDto.add(pnStatusUpdateEventRequestDto);

		return mapper.registerModule(new JavaTimeModule()).writeValueAsString(listPnStatusUpdateEventRequestDto);
	}

	protected static String getMockNotificationsRequestDto() throws JsonProcessingException {
		NotificationInfoRequestDto dto = new NotificationInfoRequestDto();
		dto.setTicketNumber("345");
		dto.setIun("ABCHFGJRENDLAPEORIFKDNSME");
		return mapper.registerModule(new JavaTimeModule()).writeValueAsString(dto);
	}

	private static String getStringFromResourse(Resource resource) throws IOException {
		return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
	}

	private static NotificationDetailsResponseDto getNotificationFromResource(Resource resource) throws IOException {
		return mapper.registerModule(new JavaTimeModule()).readValue(resource.getInputStream(),
				NotificationDetailsResponseDto.class);
	}

	private static NotificationsGeneralDataResponseDto getNotificationGeneralDataFromResource(Resource resource)
			throws IOException {
		return mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.registerModule(new JavaTimeModule())
				.readValue(resource.getInputStream(), NotificationsGeneralDataResponseDto.class);
	}

	private static GetRecipientDenominationByInternalIdResponseDto[] getRecipientInternalFromResource(Resource resource)
			throws IOException {
		return mapper.registerModule(new JavaTimeModule()).readValue(resource.getInputStream(),
				GetRecipientDenominationByInternalIdResponseDto[].class);
	}

	private static NotificationHistoryResponseDto getNotificationHistoryFromResource(Resource resource)
			throws IOException {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper.registerModule(new JavaTimeModule()).readValue(resource.getInputStream(),
				NotificationHistoryResponseDto.class);
	}

	private static FileDownloadMetadataResponseDto getFileDownloadMetadataFromResource(Resource resource)
			throws IOException {
		return mapper.registerModule(new JavaTimeModule()).readValue(resource.getInputStream(),
				FileDownloadMetadataResponseDto.class);
	}

	private static SelfCarePaDataResponseDto getSelfCarePaDataResponseFromResource(Resource resource)
			throws IOException {
		return mapper.registerModule(new JavaTimeModule()).readValue(resource.getInputStream(),
				SelfCarePaDataResponseDto.class);
	}

}
