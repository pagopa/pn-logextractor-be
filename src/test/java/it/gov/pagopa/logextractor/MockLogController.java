package it.gov.pagopa.logextractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootTest(classes = PnLogextractorBeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class MockLogController extends AbstractMock {

	@Autowired
	MockMvc mvc;

	@MockBean
	@Qualifier("simpleRestTemplate")
	RestTemplate client;
	
	@MockBean
	@Qualifier("openSearchRestTemplate")
	RestTemplate openClient;
	
	@Test
	public void test_useCase3_4_7_8() throws Exception {
		test_getPersonsLogs(3, true);
		test_getPersonsLogs(4, true);
		test_getPersonsLogs(7, false);
		test_getPersonsLogs(8, false);
	}

	public void test_getPersonsLogs(int useCase, boolean isDeanonimization) throws Exception {
		//use case 3,4,7,8
		mockPersonsLogResponseUseCase4(client, openClient);
		MockHttpServletResponse response = mvc.perform(post(personUrl).accept(APPLICATION_JSON_UTF8)
				.content(getMockPersonLogsRequestDto(useCase, isDeanonimization)).contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("password");
	}
	
	public void test_getNotificationLogs() {
		
	}

}
