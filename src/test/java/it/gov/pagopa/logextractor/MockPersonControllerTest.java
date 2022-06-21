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

@SpringBootTest(classes = PnLogextractorBeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MockPersonControllerTest extends AbstractMock {

	@Autowired
	MockMvc mvc;

	@MockBean
	@Qualifier("simpleRestTemplate")
	RestTemplate client;


	@Test
	public void test_getPersonsBasicDataWithUniqueIdentifier_ok() throws Exception {
		mockUniqueIdentifierForPerson(client);
		MockHttpServletResponse response = mvc.perform(post(identifierUrl).accept(APPLICATION_JSON_UTF8)
				.content(getMockPersonPersonIdRequestDto()).contentType(APPLICATION_JSON_UTF8)).andReturn()
				.getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("data");
		assertThat(response.getContentAsString()).contains("123");
	}

	@Test
	public void test_getPersonsBasicDataWithTaxCode_ok() throws Exception {
		mockTaxCodeForPerson200(client);
		MockHttpServletResponse response = mvc.perform(post(taxCodeUrl).accept(APPLICATION_JSON_UTF8)
				.content(getMockPersonTaxIdRequestDto()).contentType(APPLICATION_JSON_UTF8)).andReturn()
				.getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("data");
		assertThat(response.getContentAsString()).contains("BRMRSS63A02A001D");
	}
	
	@Test
	public void test_getPersonsBasicDataWithTaxCode_ko() throws Exception {
		mockTaxCodeForPerson500(client);
		MockHttpServletResponse response = mvc.perform(post(taxCodeUrl).accept(APPLICATION_JSON_UTF8)
				.content(getMockPersonTaxIdRequestDto()).contentType(APPLICATION_JSON_UTF8)).andReturn()
				.getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
		assertThat(response.getContentAsString()).contains("Errore nell'elaborazione della richiesta");

	}

}