package it.gov.pagopa.logextractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnDowntimeEntry;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnFunctionality;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnFunctionalityStatus;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnStatusResponseDto;

@SpringBootTest(classes = PnLogextractorBeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class MockDowntimeControllerTest extends AbstractMock {

	/** jUnit test for the getCurrentStatus service */

	@Test
	public void test_CheckCurretStatus() throws Exception {
		mockUniqueIdentifierForPerson();
		PnStatusResponseDto pnStatusResponseDto = new PnStatusResponseDto();
		PnDowntimeEntry itemPnDowntimeEntry = new PnDowntimeEntry();
		List<PnDowntimeEntry> listPnDowntimeEntry = new ArrayList<>();
		List<PnFunctionality> listPnFunctionality = new ArrayList<>();

		for (PnFunctionality pnFunctionality : PnFunctionality.values()) {
			listPnFunctionality.add(pnFunctionality);
		}

		itemPnDowntimeEntry.setStatus(PnFunctionalityStatus.KO);
		itemPnDowntimeEntry.setStartDate(OffsetDateTime.parse("2022-11-03T17:00:15.995Z"));
		itemPnDowntimeEntry.setFunctionality(PnFunctionality.NOTIFICATION_CREATE);
		listPnDowntimeEntry.add(itemPnDowntimeEntry);

		pnStatusResponseDto.setFunctionalities(listPnFunctionality);
		pnStatusResponseDto.setOpenIncidents(listPnDowntimeEntry);
		
		getMockPnStatusResponseDto(pnStatusResponseDto);
		
		MockHttpServletResponse response = mvc.perform(get(statusUrl).header("Auth", fakeHeader)).andReturn()
				.getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("functionalities");
		assertThat(response.getContentAsString()).contains("openIncidents");
	}

	@Test
	public void test_CheckStatusOK() throws Exception {
		mockUniqueIdentifierForPerson();
		PnStatusResponseDto pnStatusResponseDtoResponseOK = new PnStatusResponseDto();
		getMockPnStatusResponseDto(pnStatusResponseDtoResponseOK);
		MockHttpServletResponse response = mvc.perform(get(statusUrl).header("Auth", fakeHeader)).andReturn()
				.getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("functionalities");
		assertThat(response.getContentAsString()).contains("openIncidents");
	}

	@Test
	public void test_CheckStatusAuthError() throws Exception {
		mockUniqueIdentifierForPerson();
		MockHttpServletResponse response = mvc.perform(get(statusUrl)).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

	@Test
	public void test_CheckStatusLogExtractorException() throws Exception {
		mockUniqueIdentifierForPerson();
		PnStatusResponseDto pnStatusResponseDtoResponseOK = null;
		getMockPnStatusResponseDto(pnStatusResponseDtoResponseOK);
		MockHttpServletResponse response = mvc.perform(get(statusUrl).header("Auth", fakeHeader)).andReturn()
				.getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

	/** jUnit test for the addStatusChangeEvent service */

	@Test
	public void test_CheckAddStatusChangeKO() throws Exception {
		test_CheckAddStatusChange(PnFunctionalityStatus.KO);
	}

	@Test
	public void test_CheckAddStatusChangeOK() throws Exception {
		test_CheckAddStatusChange(PnFunctionalityStatus.OK);
	}

	public void test_CheckAddStatusChange(PnFunctionalityStatus pnFunctionalityStatus) throws Exception {
		mockUniqueIdentifierForPerson();
		mockAddStatusChangeEvent(client);
		MockHttpServletResponse response = mvc.perform(post(eventsUrl).accept(APPLICATION_JSON_UTF8)
				.header("Auth", fakeHeader).content(getMockPnStatusUpdateEventRequestDto(pnFunctionalityStatus))
				.contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("message");
	}

}
