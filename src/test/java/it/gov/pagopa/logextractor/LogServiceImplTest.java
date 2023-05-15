package it.gov.pagopa.logextractor;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.FileDownloadInfo;
import it.gov.pagopa.logextractor.dto.response.FileDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationHistoryResponseDto;
import it.gov.pagopa.logextractor.exception.CustomException;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.RecipientTypes;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.SessionLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.service.LogService;
import it.gov.pagopa.logextractor.service.LogServiceImpl;
import it.gov.pagopa.logextractor.service.ThreadLocalOutputStreamService;
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.constant.ResponseConstants;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationService;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationDownloadFileData;

@SpringBootTest
class LogServiceImplTest {

	@Mock
	FileUtilities fileUtils;

	@Mock
	DeanonimizationApiHandler deanonimizationApiHandler;
	@Mock
	DeanonimizationService deanonimizationService;

	@Mock
	OpenSearchApiHandler openSearchApiHandler;

	@Mock
	ThreadLocalOutputStreamService threadLocalOutputStreamService;

	@Mock
	NotificationApiHandler notificationApiHandler;

	@InjectMocks
	LogService service = new LogServiceImpl();

	BaseResponseDto genericResponse;

	@BeforeEach
	public void setUp() {
//        ReflectionTestUtils.setField(service, "downloadFileUrl", "http://localhost:3001/%s");
	}

//    @Test
//    @DisplayName("Extraction by uid with anonymization")
//    void testGetAnonymizedPersonLogs_whenProvidedDataIsValid_returnsZipArchiveWithUidLogs() throws IOException {
//        PersonLogsRequestDto dto = new PersonLogsRequestDto();
//        zipArchiveResponse.setZip(zipArchive);
//        dto.setTicketNumber("inc123");
//        dto.setDateFrom(LocalDate.now());
//        dto.setDateTo(LocalDate.now());
//        dto.setPersonId("test");
//        dto.setIun(null);
//        Mockito.when(openSearchApiHandler.getAnonymizedLogsByUid(Mockito.anyString(), Mockito.any(), Mockito.any()))
//                .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
//        Mockito.when(responseConstructor.createSimpleLogResponse(Mockito.any(), Mockito.any(), Mockito.any()))
//                .thenReturn(zipArchiveResponse);
//        DownloadArchiveResponseDto response = (DownloadArchiveResponseDto)service.getAnonymizedPersonLogs(dto,
//                "test", "test");
//        assertEquals(zipArchive, response.getZip());
//    }

	@Test
	@DisplayName("Extraction by uid with anonymization empty response")
	void testGetAnonymizedPersonLogs_whenProvidedDataIsValid_returnsEmptyResponse() throws IOException {
		PersonLogsRequestDto dto = new PersonLogsRequestDto();
		dto.setTicketNumber("inc123");
		dto.setDateFrom(LocalDate.now());
		dto.setDateTo(LocalDate.now());
		dto.setPersonId("test");
		dto.setIun(null);

		Mockito.when(threadLocalOutputStreamService.get())
				.thenReturn(new net.lingala.zip4j.io.outputstream.ZipOutputStream(new ByteArrayOutputStream()));
		Mockito.when(openSearchApiHandler.getAnonymizedLogsByUid(Mockito.anyString(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(0);

		assertThatExceptionOfType(CustomException.class).isThrownBy(() -> {
			service.getAnonymizedPersonLogs(dto, "test", "test");
		}).withMessage(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE);
	}

//    @Test
//    @DisplayName("Extraction by iun with anonymization")
//    void testGetAnonymizedPersonLogs_whenProvidedDataIsValid_returnsZipArchiveWithIunLogs() throws IOException {
//        NotificationDetailsResponseDto notificationDetails = new NotificationDetailsResponseDto();
//        notificationDetails.setDocuments(new ArrayList<>());
//        notificationDetails.setRecipients(new ArrayList<>());
//        notificationDetails.setSentAt(OffsetDateTime.now().toString());
//        PersonLogsRequestDto dto = new PersonLogsRequestDto();
//        dto.setTicketNumber("inc123");
//        dto.setDateFrom(LocalDate.now());
//        dto.setDateTo(LocalDate.now());
//        dto.setPersonId(null);
//        dto.setIun("test-test-test-111111-a-1");
//        
//        zipArchiveResponse.setZip(zipArchive);
//        Mockito.when(notificationApiHandler.getNotificationDetails(Mockito.anyString()))
//                .thenReturn(notificationDetails);
//        Mockito.when(openSearchApiHandler.getAnonymizedLogsByIun(Mockito.anyString(), Mockito.any(), Mockito.any()))
//                .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
//        Mockito.when(responseConstructor.createSimpleLogResponse(Mockito.any(), Mockito.any(), Mockito.any()))
//                .thenReturn(zipArchiveResponse);
//        DownloadArchiveResponseDto response = (DownloadArchiveResponseDto)service.getAnonymizedPersonLogs(dto,
//                "test", "test");
//        assertEquals(zipArchive, response.getZip());
//    }
//
	@Test
	@DisplayName("Empty extraction of monthly notification by iun")
	void testGetMonthlyNotifications_whenProvidedDataIsValid_returnsEmptyResponse() throws IOException,
			LogExtractorException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, ParseException {
		MonthlyNotificationsRequestDto dto = new MonthlyNotificationsRequestDto();
		dto.setEndMonth(OffsetDateTime.now());
		dto.setReferenceMonth(OffsetDateTime.now());
		dto.setPublicAuthorityName("Comune di Milano");
		dto.setTicketNumber("inc123");
		Mockito.when(threadLocalOutputStreamService.get())
				.thenReturn(new net.lingala.zip4j.io.outputstream.ZipOutputStream(new ByteArrayOutputStream()));
		Mockito.when(deanonimizationApiHandler.getPublicAuthorityId(Mockito.any())).thenReturn("test");
		Mockito.when(notificationApiHandler.getNotificationsByMonthsPeriod(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(List.of());
		Mockito.when(openSearchApiHandler.getAnonymizedLogsByUid(Mockito.anyString(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(0);
		assertThatExceptionOfType(CustomException.class).isThrownBy(() -> {
			service.getMonthlyNotifications(dto, "test", "test");
		}).withMessage(ResponseConstants.NO_NOTIFICATION_FOUND_MESSAGE);
	}

	@Test
    @DisplayName("Extraction by trace id with empty response")
    void testGetTraceIdLogs_whenProvidedDataIsValid_returnsEmptyResponse() throws IOException, LogExtractorException {
        TraceIdLogsRequestDto dto = new TraceIdLogsRequestDto();
        dto.setDateFrom(LocalDate.now());
        dto.setDateTo(LocalDate.now());
        dto.setTraceId("123");
        Mockito.when(openSearchApiHandler.getAnonymizedLogsByTraceId(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(0);
        assertThatExceptionOfType(CustomException.class).isThrownBy(() -> { 
        	service.getTraceIdLogs(dto, "test", "test");
        })
        .withMessage(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE); 
    }

	@Test
  @DisplayName("Notification data extraction by iun with files not ready to be downloaded")
  void testGetNotificationInfoLogs_whenProvidedDataIsValid_returnsTimeToWait() throws IOException {
    NotificationInfoRequestDto dto = new NotificationInfoRequestDto();
    dto.setIun("test-test-test-111111-a-1");
    dto.setTicketNumber("inc123");
    NotificationDetailsResponseDto notificationDetails = new NotificationDetailsResponseDto();
    notificationDetails.setDocuments(new ArrayList<>());
    notificationDetails.setRecipients(new ArrayList<>());
    notificationDetails.setSentAt(OffsetDateTime.now().toString());
    NotificationHistoryResponseDto notificationHistoryResponseDto = new NotificationHistoryResponseDto();
    notificationHistoryResponseDto.setTimeline(new ArrayList<>());
    ArrayList<NotificationDownloadFileData> downloadFileDataArrayList = new ArrayList<>(
        List.of(new NotificationDownloadFileData("test", "test", "test")));
    FileDownloadMetadataResponseDto fileDownloadMetadataResponseDto = new FileDownloadMetadataResponseDto();
    FileDownloadInfo fileDownloadInfo = new FileDownloadInfo();
    fileDownloadInfo.setUrl(null);
    fileDownloadInfo.setRetryAfter(60);
    fileDownloadMetadataResponseDto.setDownload(fileDownloadInfo);

    Mockito.when(notificationApiHandler.getNotificationDetails(Mockito.anyString()))
        .thenReturn(notificationDetails);
    Mockito.when(notificationApiHandler.getNotificationHistory(Mockito.any(), Mockito.anyInt(),
            Mockito.any()))
        .thenReturn(notificationHistoryResponseDto);
    Mockito.when(notificationApiHandler.getLegalFactFileDownloadData(Mockito.any()))
        .thenReturn(downloadFileDataArrayList);
    Mockito.when(notificationApiHandler.getNotificationDocumentFileDownloadData(Mockito.any()))
        .thenReturn(downloadFileDataArrayList);
    Mockito.when(notificationApiHandler.getPaymentFilesDownloadData(Mockito.any()))
        .thenReturn(downloadFileDataArrayList);
    Mockito.when(notificationApiHandler.getDownloadMetadata(Mockito.any()))
        .thenReturn(fileDownloadMetadataResponseDto);

	assertThatExceptionOfType(CustomException.class).isThrownBy(() -> { 
        	service.getNotificationInfoLogs(dto, "test", "test");
        })
        .withMessage(ResponseConstants.OPERATION_CANNOT_BE_COMPLETED_MESSAGE+"1 minuto"); 
    }

	@Test
	@DisplayName("Extraction by uid with de-anonymization empty response")
	void testGetDeanonymizedPersonLogs_whenProvidedDataIsValid_returnsEmptyResponse()
			throws IOException, LogExtractorException {
		PersonLogsRequestDto dto = new PersonLogsRequestDto();
		dto.setTicketNumber("inc123");
		dto.setDateFrom(LocalDate.now());
		dto.setDateTo(LocalDate.now());
		dto.setTaxId("test");
		dto.setRecipientType(RecipientTypes.PF);
		dto.setIun(null);
		Mockito.when(deanonimizationApiHandler.getUniqueIdentifierForPerson(Mockito.any(), Mockito.any()))
				.thenReturn("test");
		Mockito.when(openSearchApiHandler.getAnonymizedLogsByUid(Mockito.anyString(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(0);
//        Mockito.when(deanonimizationApiHandler.deanonimizeDocuments(Mockito.any(), Mockito.any(), Mockito.any()))
//                .thenReturn(0);
		assertThatExceptionOfType(CustomException.class).isThrownBy(() -> {
			service.getAnonymizedPersonLogs(dto, "test", "test");
		}).withMessage(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE);
	}

	@Test
	@DisplayName("Anonymized empty extraction by JTI")
	void testGetAnonimizedSessionLogs_whenProvidedDataIsValid_returnsEmptyResponse() throws IOException {
		SessionLogsRequestDto dto = new SessionLogsRequestDto();
		dto.setDateFrom(LocalDate.now());
		dto.setDateTo(LocalDate.now());
		dto.setDeanonimization(false);
		dto.setJti("12954F907C0535ABE97F761829C6BD11");
		dto.setTicketNumber("123");
		Mockito.when(openSearchApiHandler.getAnonymizedSessionLogsByJti(Mockito.anyString(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(0);
		assertThatExceptionOfType(CustomException.class).isThrownBy(() -> {
			service.getAnonymizedSessionLogs(dto, "test", "test");
		}).withMessage(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE);
	}

	@Test
    @DisplayName("Empty de-anonymized extraction by JTI")
    void testGetDeanonimizedSessionLogs_whenProvidedDataIsValid_returnsEmptyResponse() throws IOException, LogExtractorException {
        SessionLogsRequestDto dto = new SessionLogsRequestDto();
        dto.setDateFrom(LocalDate.now());
        dto.setDateTo(LocalDate.now());
        dto.setDeanonimization(true);
        dto.setJti("12954F907C0535ABE97F761829C6BD11");
        dto.setTicketNumber("123");
        Mockito.when(openSearchApiHandler.getAnonymizedSessionLogsByJti(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(0);
        assertThatExceptionOfType(CustomException.class).isThrownBy(() -> { 
        	service.getDeanonimizedSessionLogs(dto, "test", "test");
        })
        .withMessage(ResponseConstants.NO_DOCUMENT_FOUND_MESSAGE); 
    }
}
