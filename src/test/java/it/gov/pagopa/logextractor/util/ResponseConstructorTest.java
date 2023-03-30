package it.gov.pagopa.logextractor.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.gov.pagopa.logextractor.dto.response.DownloadArchiveResponseDto;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationDownloadFileData;

class ResponseConstructorTest {

    @Test
    @DisplayName("Create zip archive with notification's related files")
    void testCreateNotificationLogResponse_whenAllFilesAreProvided_returnsCreatedZipArchive() throws IOException {
        ResponseConstructor responseConstructor = new ResponseConstructor();
        List<String> openSearchLogs = List.of("{\"test\":\"test\"}", "{\"test1\":\"test1\"}");
        List<File> filesToAdd = new ArrayList<>();
        List<NotificationDownloadFileData> filesNotDownloadable =List.of(
                new NotificationDownloadFileData("test", "test", "test"),
                new NotificationDownloadFileData("test1", "test1", "test1")
        );

        FileUtilities fileUtils = new FileUtilities();

        File logFile = fileUtils.writeTxt(openSearchLogs, GenericConstants.LOG_FILE_NAME);
        filesToAdd.add(logFile);

        DownloadArchiveResponseDto output = responseConstructor.createNotificationLogResponse( filesToAdd,
                filesNotDownloadable, "test");
        Assertions.assertNotNull(output, () -> "Output zip archive shouldn't be null");
    }
}
