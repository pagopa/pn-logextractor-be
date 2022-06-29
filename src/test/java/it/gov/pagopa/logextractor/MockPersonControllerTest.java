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

import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootTest(classes = PnLogextractorBeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MockPersonControllerTest extends AbstractMock {

	public void mvcPostPerform(String url, String body, String key, String value, HttpStatus responseCode) throws JsonProcessingException, Exception {
		MockHttpServletResponse response = mvc.perform(post(url).accept(APPLICATION_JSON_UTF8).header("Auth", fakeHeader)
				.content(body).contentType(APPLICATION_JSON_UTF8)).andReturn()
				.getResponse();
		assertThat(response.getStatus()).isEqualTo(responseCode.value());
		assertThat(response.getContentAsString()).contains(key);
		assertThat(response.getContentAsString()).contains(value);
	}

	@Test
	public void test_getPersonsBasicDataWithUniqueIdentifier_ok() throws Exception {
		mockUniqueIdentifierForPerson();
		mvcPostPerform(identifierUrl, getMockPersonPersonIdRequestDto(), "data", "123", HttpStatus.OK);
	}

	@Test
	public void test_getPersonsBasicDataWithTaxCode_ok() throws Exception {
		mockUniqueIdentifierForPerson();
		mockTaxCodeForPerson200();
		mvcPostPerform(taxCodeUrl, getMockPersonTaxIdRequestDto(), "data", "BRMRSS63A02A001D", HttpStatus.OK);
	}
	
	@Test
	public void test_getPersonsBasicDataWithTaxCode_5xx() throws Exception {
		mockUniqueIdentifierForPerson();
		mockTaxCodeForPersonServerError(HttpStatus.INTERNAL_SERVER_ERROR);
		String errorResponse = "Errore nell'elaborazione della richiesta";
		mvcPostPerform(taxCodeUrl, getMockPersonTaxIdRequestDto(), errorResponse, errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@Test
	public void test_getPersonsBasicDataWithTaxCode_4xx() throws Exception {
		mockUniqueIdentifierForPerson();
		mockTaxCodeForPersonClientError(HttpStatus.METHOD_NOT_ALLOWED);
		String errorResponse = "Errore nell'elaborazione della richiesta";
		mvcPostPerform(taxCodeUrl, getMockPersonTaxIdRequestDto(), errorResponse, errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}