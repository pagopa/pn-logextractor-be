package it.gov.pagopa.logextractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootTest(classes = PnLogextractorBeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableWebMvc
class MockLogControllerTest extends AbstractMock {
	
	@Test
	void test_healthcheck() throws Exception {
		mockMissingUniqueIdentifierForPerson();
		MockHttpServletResponse response = mvc
				.perform(get(healthcheckUrl).accept(APPLICATION_JSON_UTF8).headers(getHeaders())
						.contentType(APPLICATION_JSON_UTF8))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());	
	}
	

	@Test
	void test_useCase3_4_7_8() throws Exception {
		test_getPersonsLogsPA(3, true, jsonDocSearchPA);
		test_getPersonsLogs(3, true, jsonDocSearchPF);
		test_getPersonsLogs(4, true, jsonDocSearchPF);
		test_getPersonsLogs(7, false, jsonDocSearchPF);
		test_getPersonsLogs(8, false, jsonDocSearchPF);
	}

	@Test
	void test_useCase10() throws Exception {
		test_getProcesses(LocalDate.now(), LocalDate.now(), "traceId");
	}

	void test_getPersonsLogsPA(int useCase, boolean isDeanonimization, String json) throws Exception {
		// use case 3 PA
		mockPersonsLogResponse(json);
		mockPublicAuthorityName();
		mockTaxCodeForPerson();
		MockHttpServletResponse response = mvc.perform(post(personUrl)
				.headers(getHeaders()).content(getMockPersonLogsRequestDto(useCase, isDeanonimization))
				.contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getHeaderNames()).contains("password");
	}

	void test_getPersonsLogs(int useCase, boolean isDeanonimization, String json) throws Exception {
		// use case 3,4,7,8
		mockPersonsLogResponse(json);
		mockTaxCodeForPerson();
		MockHttpServletResponse response = mvc.perform(post(personUrl)
				.headers(getHeaders()).content(getMockPersonLogsRequestDto(useCase, isDeanonimization))
				.contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getHeaderNames()).contains("password");
	}
	
	@Test
	void test_case4EmptyOpenSearchResponse() throws Exception {
		// use case 4 
		mockPersonsLogResponse(jsonEmptyDocSearchPF);
		mockTaxCodeForPerson();
		MockHttpServletResponse response = mvc.perform(post(personUrl)
				.headers(getHeaders()).content(getMockPersonLogsRequestDto(4, true))
				.contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}

	@Test
	void test_getNotificationLogs() throws Exception {
		// use case 6
		mockPublicAuthorityIdAndNotificationsBetweenMonths(false);
		mockNotificationResponse();
		mockPersonsLogUseCase6Response();
		MockHttpServletResponse response = mvc
				.perform(post(notificationUrl).headers(getHeaders())
						.content(getMockMonthlyNotificationsRequestDto()).contentType(APPLICATION_JSON_UTF8))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getHeaderNames()).contains("password");
	}
	
	@Test
	void test_getNotificationLogsNotificationsIsEmpty() throws Exception {
		mockPublicAuthorityIdAndNotificationsBetweenMonths(true);
		mockPersonsLogUseCase6Response();
		MockHttpServletResponse response = mvc
				.perform(post(notificationUrl).headers(getHeaders())
						.content(getMockMonthlyNotificationsRequestDto()).contentType(APPLICATION_JSON_UTF8))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
	
	@Test
	void test_getPersonsLogsOpenSearchResponseIsEmpty() throws Exception {
		mockPersonsLogResponse(jsonDocSearchPF);
		mockTaxCodeForPerson();
		MockHttpServletResponse response = mvc
				.perform(post(personUrl).headers(getHeaders())
						.content(getMockPersonLogsRequestDtoPersonIdNull()).contentType(APPLICATION_JSON_UTF8))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
	
	@Test
	void test_getSessionLogsOpenSearchResponseAnonymized() throws Exception {	
		mockPersonsLogResponse(jsonDocSearchPF);
		mockTaxCodeForPerson();
		MockHttpServletResponse response = mvc.perform(post(sessionUrl)
				.headers(getHeaders()).content(getMockSessionLogsRequestDto(false))
				.contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getHeaderNames()).contains("password");
	}
	
	@Test
	void test_getSessionLogsOpenSearchResponseDeanonymized() throws Exception {	
		mockPersonsLogResponse(jsonDocSearchPF);
		mockTaxCodeForPerson();
		MockHttpServletResponse response = mvc.perform(post(sessionUrl)
				.headers(getHeaders()).content(getMockSessionLogsRequestDto(true))
				.contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getHeaderNames()).contains("password");
	}
	
	@Test
	void test_getSessionLogsOpenSearchResponseAnonymizedWithEmptySearch() throws Exception {	
		mockEmptyPersonsLogResponse(jsonDocSearchPF);
		mockTaxCodeForPerson();
		MockHttpServletResponse response = mvc.perform(post(sessionUrl)
				.headers(getHeaders()).content(getMockSessionLogsRequestDto(false))
				.contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
	
	@Test
	void test_getSessionLogsOpenSearchResponseDeanonymizedWithEmptySearch() throws Exception {	
		mockEmptyPersonsLogResponse(jsonDocSearchPF);
		mockTaxCodeForPerson();
		MockHttpServletResponse response = mvc.perform(post(sessionUrl)
				.headers(getHeaders()).content(getMockSessionLogsRequestDto(true))
				.contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}

	@Test
	void test_getNotificationInfoLogs() throws Exception {
		mockNotificationDetailsResponse();
		mockNotificationHistoryResponse();
		mockFileDownloadMetadataResponseDTO(mockFileKey);
		mockDocumentsByMultiSearchQuery();
		mockPersonsLogResponse(jsonDocSearchPF);
		MockHttpServletResponse response = mvc
				.perform(post(notificationInfoUrl).headers(getHeaders())
						.content(getMockNotificationsRequestDto()).contentType(APPLICATION_JSON_UTF8))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
	}

	void test_getProcesses(LocalDate dateFrom, LocalDate dateTo, String traceId) throws Exception {
		mockPersonsLogResponse(jsonDocSearchPF);
		MockHttpServletResponse response = mvc.perform(post(processesUrl)
				.headers(getHeaders()).content(getMockTraceIdLogsRequestDto(dateFrom, dateTo, traceId))
				.contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getHeaderNames()).contains("password");
	}

}
