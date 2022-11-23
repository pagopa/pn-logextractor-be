package it.gov.pagopa.logextractor.util;

import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationDownloadFileData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ResponseConstructorTest {

    @Test
    @DisplayName("Create zip archive with notification's related files")
    void testCreateNotificationLogResponse_whenAllFilesAreProvided_returnsCreatedZipArchive() throws IOException {
        List<String> openSearchLogs = List.of("{\"test\":\"test\"}", "{\"test1\":\"test1\"}");
        List<File> filesToAdd = new ArrayList<>();
        List<NotificationDownloadFileData> filesNotDownloadable =List.of(
                new NotificationDownloadFileData("test", "test", "test"),
                new NotificationDownloadFileData("test1", "test1", "test1")
        );

        DownloadArchiveResponseDto output = ResponseConstructor.createNotificationLogResponse(openSearchLogs,
                filesToAdd, filesNotDownloadable, "test", "test");
        Assertions.assertNotNull(output, () -> "Output zip archive shouldn't be null");
    }
}