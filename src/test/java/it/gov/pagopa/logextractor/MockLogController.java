package it.gov.pagopa.logextractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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

import it.gov.pagopa.logextractor.rest.LogController;
import it.gov.pagopa.logextractor.service.LogServiceImpl;

@SpringBootTest(classes = PnLogextractorBeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class MockLogController extends AbstractMock {

	@Autowired
	MockMvc mvc;

	@InjectMocks
	LogController logController;

	@InjectMocks
	LogServiceImpl logService;

	@MockBean
	@Qualifier("simpleRestTemplate")
	RestTemplate client;

	@Test
	public void test_getPersons() throws Exception {
		//TODO incomplete 
		MockHttpServletResponse response = mvc.perform(post(personUrl).accept(APPLICATION_JSON_UTF8)
				.content(getMockPersonLogsRequestDto()).contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("data");
		assertThat(response.getContentAsString()).contains("123");
	}

}
