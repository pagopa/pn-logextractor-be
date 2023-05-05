package it.gov.pagopa.logextractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import it.gov.pagopa.logextractor.dto.FileDownloadInfo;
import it.gov.pagopa.logextractor.dto.NotificationData;
import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.dto.response.FileDownloadMetadataResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationDetailsResponseDto;
import it.gov.pagopa.logextractor.dto.response.NotificationHistoryResponseDto;
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
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.ResponseConstructor;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.DeanonimizationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationApiHandler;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationDownloadFileData;
import it.gov.pagopa.logextractor.util.external.s3.S3ApiHandler;

@SpringBootTest
class LogServiceImplTest {

  @Mock
  FileUtilities fileUtils;

  @Mock
  DeanonimizationApiHandler deanonimizationApiHandler;

  @Mock
  OpenSearchApiHandler openSearchApiHandler;

  @Mock
  ResponseConstructor responseConstructor;

  @Mock
  NotificationApiHandler notificationApiHandler;

  @Mock
  S3ApiHandler s3ApiHandler;
  @InjectMocks
  LogService service = new LogServiceImpl();

  byte[] zipArchive = "test".getBytes();

  DownloadArchiveResponseDto zipArchiveResponse = new DownloadArchiveResponseDto();
  BaseResponseDto genericResponse;

  @Test
  @DisplayName("Extraction by uid with anonymization")
  void testGetAnonymizedPersonLogs_whenProvidedDataIsValid_returnsZipArchiveWithUidLogs()
      throws IOException {
    PersonLogsRequestDto dto = new PersonLogsRequestDto();
    zipArchiveResponse.setZip(zipArchive);
    dto.setTicketNumber("inc123");
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setPersonId("test");
    dto.setIun(null);
    Mockito.when(openSearchApiHandler.getAnonymizedLogsByUid(Mockito.anyString(), Mockito.any(),
            Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(
            responseConstructor.createSimpleLogResponse(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(zipArchiveResponse);
    DownloadArchiveResponseDto response = (DownloadArchiveResponseDto) service.getAnonymizedPersonLogs(
        dto,
        "test", "test");
    assertEquals(zipArchive, response.getZip());
  }

  @Test
  @DisplayName("Extraction by uid with anonymization empty response")
  void testGetAnonymizedPersonLogs_whenProvidedDataIsValid_returnsEmptyResponse()
      throws IOException {
    PersonLogsRequestDto dto = new PersonLogsRequestDto();
    dto.setTicketNumber("inc123");
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setPersonId("test");
    dto.setIun(null);
    Mockito.when(openSearchApiHandler.getAnonymizedLogsByUid(Mockito.anyString(), Mockito.any(),
            Mockito.any()))
        .thenReturn(List.of());
    genericResponse = service.getAnonymizedPersonLogs(dto, "test", "test");
    assertEquals("Nessun documento trovato per i dati inseriti",
        service.getAnonymizedPersonLogs(dto, "test", "test").getMessage());
  }

  @Test
  @DisplayName("Extraction by iun with anonymization")
  void testGetAnonymizedPersonLogs_whenProvidedDataIsValid_returnsZipArchiveWithIunLogs()
      throws IOException {
    NotificationDetailsResponseDto notificationDetails = new NotificationDetailsResponseDto();
    notificationDetails.setDocuments(new ArrayList<>());
    notificationDetails.setRecipients(new ArrayList<>());
    notificationDetails.setSentAt(OffsetDateTime.now().toString());
    PersonLogsRequestDto dto = new PersonLogsRequestDto();
    dto.setTicketNumber("inc123");
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setPersonId(null);
    dto.setIun("test-test-test-111111-a-1");
    zipArchiveResponse.setZip(zipArchive);
    Mockito.when(notificationApiHandler.getNotificationDetails(Mockito.anyString()))
        .thenReturn(notificationDetails);
    Mockito.when(openSearchApiHandler.getAnonymizedLogsByIun(Mockito.anyString(), Mockito.any(),
            Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(
            responseConstructor.createSimpleLogResponse(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(zipArchiveResponse);
    DownloadArchiveResponseDto response = (DownloadArchiveResponseDto) service.getAnonymizedPersonLogs(
        dto,
        "test", "test");
    assertEquals(zipArchive, response.getZip());
  }

  @Test
  @DisplayName("Empty extraction of monthly notification by iun")
  void testGetMonthlyNotifications_whenProvidedDataIsValid_returnsEmptyResponse()
      throws IOException, LogExtractorException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, ParseException {
    MonthlyNotificationsRequestDto dto = new MonthlyNotificationsRequestDto();
    dto.setEndMonth(OffsetDateTime.now());
    dto.setReferenceMonth(OffsetDateTime.now());
    dto.setPublicAuthorityName("Comune di Milano");
    dto.setTicketNumber("inc123");
    Mockito.when(deanonimizationApiHandler.getPublicAuthorityId(Mockito.any())).thenReturn("test");
    Mockito.when(notificationApiHandler.getNotificationsByMonthsPeriod(Mockito.any(), Mockito.any(),
            Mockito.any()))
        .thenReturn(List.of());
    assertEquals("Nessuna notifica trovata per i dati inseriti",
        service.getMonthlyNotifications(dto, "test", "test").getMessage());
  }

  @Test
  @DisplayName("Extraction of monthly notification by iun")
  void testGetMonthlyNotifications_whenProvidedDataIsValid_returnsZipArchive()
      throws IOException, LogExtractorException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, ParseException {
    MonthlyNotificationsRequestDto dto = new MonthlyNotificationsRequestDto();
    dto.setEndMonth(OffsetDateTime.now());
    dto.setReferenceMonth(OffsetDateTime.now());
    dto.setPublicAuthorityName("Comune di Milano");
    dto.setTicketNumber("inc123");
    NotificationData notificationData = new NotificationData();
    notificationData.setSubject("test");
    notificationData.setSentAt("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}");
    notificationData.setRequestAcceptedAt("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}");
    notificationData.setSubject("test");
    notificationData.setRecipients(new ArrayList<>());
    File testFile = new File("test");
    ArrayList<NotificationData> notificationDataArrayList = new ArrayList<>(
        List.of(notificationData));
    zipArchiveResponse.setZip(zipArchive);
    Mockito.when(deanonimizationApiHandler.getPublicAuthorityId(Mockito.any())).thenReturn("test");
    Mockito.when(notificationApiHandler.getNotificationsByMonthsPeriod(Mockito.any(), Mockito.any(),
            Mockito.any()))
        .thenReturn(notificationDataArrayList);
    Mockito.when(fileUtils.getFileWithRandomName(Mockito.any(), Mockito.any()))
        .thenReturn(testFile);
    Mockito.when(fileUtils.toCsv(notificationDataArrayList)).thenReturn(List.of());
    Mockito.doNothing().when(fileUtils).writeCsv(Mockito.any(), Mockito.any());
    Mockito.when(responseConstructor.createCsvFileResponse(Mockito.any(), Mockito.any()))
        .thenReturn(zipArchiveResponse);
    DownloadArchiveResponseDto response = (DownloadArchiveResponseDto) service.getMonthlyNotifications(
        dto,
        "test", "test");
    assertEquals(zipArchive, response.getZip());
  }

  @Test
  @DisplayName("Extraction by trace id with empty response")
  void testGetTraceIdLogs_whenProvidedDataIsValid_returnsEmptyResponse()
      throws IOException, LogExtractorException {
    TraceIdLogsRequestDto dto = new TraceIdLogsRequestDto();
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setTraceId("123");
    Mockito.when(openSearchApiHandler.getAnonymizedLogsByTraceId(Mockito.anyString(), Mockito.any(),
            Mockito.any()))
        .thenReturn(List.of());
    assertEquals("Nessun documento trovato per i dati inseriti",
        service.getTraceIdLogs(dto, "test", "test").getMessage());
  }

  @Test
  @DisplayName("Extraction by trace id")
  void testGetTraceIdLogs_whenProvidedDataIsValid_returnsZipArchive()
      throws IOException, LogExtractorException {
    TraceIdLogsRequestDto dto = new TraceIdLogsRequestDto();
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setTraceId("123");
    zipArchiveResponse.setZip(zipArchive);
    Mockito.when(openSearchApiHandler.getAnonymizedLogsByTraceId(Mockito.anyString(), Mockito.any(),
            Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(
            responseConstructor.createSimpleLogResponse(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(zipArchiveResponse);
    DownloadArchiveResponseDto response = (DownloadArchiveResponseDto) service.getTraceIdLogs(dto,
        "test", "test");
    assertEquals(zipArchive, response.getZip());
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

    assertEquals("L'operazione non può essere ancora completata, ritentare tra 1 minuto",
        service.getNotificationInfoLogs(dto, "test", "test").getMessage());
  }

  @Test
  @DisplayName("Notification data extraction by iun with files ready to be downloaded")
  void testGetNotificationInfoLogs_whenProvidedDataIsValid_returnsZipArchive() throws IOException {
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
    fileDownloadInfo.setUrl("test");
    fileDownloadInfo.setRetryAfter(null);
    fileDownloadMetadataResponseDto.setDownload(fileDownloadInfo);
    zipArchiveResponse.setZip(zipArchive);

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
    Mockito.when(fileUtils.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(new File("test"));
    Mockito.when(openSearchApiHandler.getAnonymizedLogsByIun(Mockito.anyString(), Mockito.any(),
            Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(fileUtils.writeTxt(Mockito.any(), Mockito.anyString()))
        .thenReturn(new File("test"));
    Mockito.when(responseConstructor.createNotificationLogResponse(Mockito.any(), Mockito.any(),
            Mockito.any()))
        .thenReturn(zipArchiveResponse);
    DownloadArchiveResponseDto response = (DownloadArchiveResponseDto) service.getNotificationInfoLogs(
        dto,
        "test", "test");
    assertEquals(zipArchive, response.getZip());
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
    Mockito.when(
            deanonimizationApiHandler.getUniqueIdentifierForPerson(Mockito.any(), Mockito.any()))
        .thenReturn("test");
    Mockito.when(openSearchApiHandler.getAnonymizedLogsByUid(Mockito.anyString(), Mockito.any(),
            Mockito.any()))
        .thenReturn(List.of());
    Mockito.when(deanonimizationApiHandler.deanonimizeDocuments(Mockito.any(), Mockito.any()))
        .thenReturn(List.of());
    genericResponse = service.getDeanonimizedPersonLogs(dto, "test", "test");
    assertEquals("Nessun documento trovato per i dati inseriti",
        service.getAnonymizedPersonLogs(dto, "test", "test").getMessage());
  }

  @Test
  @DisplayName("Extraction by uid with de-anonymization response")
  void testGetDeanonymizedPersonLogs_whenProvidedDataIsValid_returnsZipArchiveWithUidLogs()
      throws IOException, LogExtractorException {
    PersonLogsRequestDto dto = new PersonLogsRequestDto();
    dto.setTicketNumber("inc123");
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setTaxId("test");
    dto.setRecipientType(RecipientTypes.PF);
    zipArchiveResponse.setZip(zipArchive);
    Mockito.when(
            deanonimizationApiHandler.getUniqueIdentifierForPerson(Mockito.any(), Mockito.any()))
        .thenReturn("test");
    Mockito.when(openSearchApiHandler.getAnonymizedLogsByUid(Mockito.anyString(), Mockito.any(),
            Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(deanonimizationApiHandler.deanonimizeDocuments(Mockito.any(), Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(fileUtils.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(new File("test"));
    Mockito.when(fileUtils.writeTxt(Mockito.any(), Mockito.any()))
        .thenReturn(new File("test"));
    Mockito.when(responseConstructor.createNotificationLogResponse(Mockito.any(), Mockito.any(),
            Mockito.any()))
        .thenReturn(zipArchiveResponse);
    DownloadArchiveResponseDto response = (DownloadArchiveResponseDto) service.getDeanonimizedPersonLogs(
        dto,
        "test", "test");
    assertEquals(zipArchive, response.getZip());
  }

  @Test
  @DisplayName("Extraction by iun with de-anonymization response")
  void testGetDeanonymizedPersonLogs_whenProvidedDataIsValid_returnsZipArchiveWithIunLogs()
      throws IOException, LogExtractorException {
    NotificationDetailsResponseDto notificationDetails = new NotificationDetailsResponseDto();
    notificationDetails.setDocuments(new ArrayList<>());
    notificationDetails.setRecipients(new ArrayList<>());
    notificationDetails.setSentAt(OffsetDateTime.now().toString());
    PersonLogsRequestDto dto = new PersonLogsRequestDto();
    dto.setTicketNumber("inc123");
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setIun("test-test-test-111111-a-1");
    zipArchiveResponse.setZip(zipArchive);
    Mockito.when(notificationApiHandler.getNotificationDetails(Mockito.anyString()))
        .thenReturn(notificationDetails);
    Mockito.when(openSearchApiHandler.getAnonymizedLogsByIun(Mockito.anyString(), Mockito.any(),
            Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(deanonimizationApiHandler.deanonimizeDocuments(Mockito.any(), Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(
            responseConstructor.createSimpleLogResponse(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(zipArchiveResponse);
    DownloadArchiveResponseDto response = (DownloadArchiveResponseDto) service.getDeanonimizedPersonLogs(
        dto,
        "test", "test");
    assertEquals(zipArchive, response.getZip());
  }

  @Test
  @DisplayName("Anonymized extraction by JTI")
  void testGetAnonimizedSessionLogs_whenProvidedDataIsValid_returnsZipArchive() throws IOException {
    SessionLogsRequestDto dto = new SessionLogsRequestDto();
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setDeanonimization(false);
    dto.setJti("12954F907C0535ABE97F761829C6BD11");
    dto.setTicketNumber("123");
    zipArchiveResponse.setZip(zipArchive);
    Mockito.when(
            openSearchApiHandler.getAnonymizedSessionLogsByJti(Mockito.anyString(), Mockito.any(),
                Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(
            responseConstructor.createSimpleLogResponse(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(zipArchiveResponse);
    DownloadArchiveResponseDto response = (DownloadArchiveResponseDto) service.getAnonymizedSessionLogs(
        dto,
        "test", "test");
    assertEquals(zipArchive, response.getZip());
  }

  @Test
  @DisplayName("Anonymized empty extraction by JTI")
  void testGetAnonimizedSessionLogs_whenProvidedDataIsValid_returnsEmptyResponse()
      throws IOException {
    SessionLogsRequestDto dto = new SessionLogsRequestDto();
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setDeanonimization(false);
    dto.setJti("12954F907C0535ABE97F761829C6BD11");
    dto.setTicketNumber("123");
    Mockito.when(
            openSearchApiHandler.getAnonymizedSessionLogsByJti(Mockito.anyString(), Mockito.any(),
                Mockito.any()))
        .thenReturn(List.of());
    assertEquals("Nessun documento trovato per i dati inseriti",
        service.getAnonymizedSessionLogs(dto, "test", "test").getMessage());
  }

  @Test
  @DisplayName("Extraction by JTI with deanonymization")
  void testGetDeanonimizedSessionLogs_whenProvidedDataIsValid_returnsZipArchive()
      throws IOException, LogExtractorException {
    SessionLogsRequestDto dto = new SessionLogsRequestDto();
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setDeanonimization(true);
    dto.setJti("12954F907C0535ABE97F761829C6BD11");
    dto.setTicketNumber("123");
    zipArchiveResponse.setZip(zipArchive);
    Mockito.when(
            openSearchApiHandler.getAnonymizedSessionLogsByJti(Mockito.anyString(), Mockito.any(),
                Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(deanonimizationApiHandler.deanonimizeDocuments(Mockito.any(), Mockito.any()))
        .thenReturn(List.of("{\"@timestamp\":\"2023-01-10T12:10:15.300Z\"}"));
    Mockito.when(fileUtils.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(new File("test"));
    Mockito.when(fileUtils.writeTxt(Mockito.any(), Mockito.any()))
        .thenReturn(new File("test"));
    Mockito.when(responseConstructor.createNotificationLogResponse(Mockito.any(), Mockito.any(),
            Mockito.any()))
        .thenReturn(zipArchiveResponse);
    Mockito.when(s3ApiHandler.getFile(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(new File("export/test"));
    DownloadArchiveResponseDto response = (DownloadArchiveResponseDto) service.getDeanonimizedSessionLogs(
        dto,
        "test", "test");
    assertEquals(zipArchive, response.getZip());
  }

  @Test
  @DisplayName("Empty de-anonymized extraction by JTI")
  void testGetDeanonimizedSessionLogs_whenProvidedDataIsValid_returnsEmptyResponse()
      throws IOException, LogExtractorException {
    SessionLogsRequestDto dto = new SessionLogsRequestDto();
    dto.setDateFrom(LocalDate.now());
    dto.setDateTo(LocalDate.now());
    dto.setDeanonimization(true);
    dto.setJti("12954F907C0535ABE97F761829C6BD11");
    dto.setTicketNumber("123");
    Mockito.when(
            openSearchApiHandler.getAnonymizedSessionLogsByJti(Mockito.anyString(), Mockito.any(),
                Mockito.any()))
        .thenReturn(List.of());
    Mockito.when(deanonimizationApiHandler.deanonimizeDocuments(Mockito.any(), Mockito.any()))
        .thenReturn(List.of());
    assertEquals("Nessun documento trovato per i dati inseriti",
        service.getDeanonimizedSessionLogs(dto, "test", "test").getMessage());
  }
}
