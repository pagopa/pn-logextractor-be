package it.gov.pagopa.logextractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootTest(classes = PnLogextractorBeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class MockLogController extends AbstractMock {
	
//	@Test
//	public void test_useCase3_4_7_8() throws Exception {
//		test_getPersonsLogs(3, true);
//		test_getPersonsLogs(4, true);
//		test_getPersonsLogs(7, false);
//		test_getPersonsLogs(8, false);
//	}
//	
//	@Test
//	public void test_useCase10() throws Exception {
//		test_getProcesses("dateFrom", "dateTo", "ticketNumber", "traceId");
//	}

//	public void test_getPersonsLogs(int useCase, boolean isDeanonimization) throws Exception {
//		//use case 3,4,7,8
//		mockPersonsLogResponse();
//		MockHttpServletResponse response = mvc.perform(post(personUrl).accept(APPLICATION_JSON_UTF8).header("Auth", fakeHeader)
//				.content(getMockPersonLogsRequestDto(useCase, isDeanonimization)).contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
//		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
//		assertThat(response.getContentAsString()).contains("password");
//	}
//	
//	@Test
//	public void test_getNotificationLogs() throws JsonProcessingException, Exception {
//		//use case 6
//		mockNotificationResponse();
//		mockPersonsLogUseCase6Response();
//		MockHttpServletResponse response = mvc.perform(post(notificationUrl).accept(APPLICATION_JSON_UTF8).header("Auth", fakeHeader)
//				.content(getMockMonthlyNotificationsRequestDto()).contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
//		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
//		assertThat(response.getContentAsString()).contains("password");
//	}
//	
//	@Test
//	public void test_getNotificationInfoLogs() throws JsonProcessingException, Exception {
//		mockNotificationResponse();
//		mockPersonsLogResponse();
//		MockHttpServletResponse response = mvc.perform(post(notificationInfoUrl).accept(APPLICATION_JSON_UTF8).header("Auth", fakeHeader)
//				.content(getMockNotificationsRequestDto()).contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
//		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
//		assertThat(response.getContentAsString()).contains("password");
//	}
//
//
//	public void test_getProcesses(String dateFrom, String dateTo, String ticketNumber, String traceId) throws JsonProcessingException, Exception {
//		mockPersonsLogResponse();
//		MockHttpServletResponse response = mvc.perform(post(processesUrl).accept(APPLICATION_JSON_UTF8).header("Auth", fakeHeader)
//				.content(getMockTraceIdLogsRequestDto(dateFrom, dateTo, ticketNumber,  traceId)).contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
//		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
//		assertThat(response.getContentAsString()).contains("password");
//	}

}
