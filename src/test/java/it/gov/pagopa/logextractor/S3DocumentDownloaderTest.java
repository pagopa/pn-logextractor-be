package it.gov.pagopa.logextractor;

import it.gov.pagopa.logextractor.service.ZipInfo;
import it.gov.pagopa.logextractor.service.ZipService;
import it.gov.pagopa.logextractor.util.external.s3.S3DocumentDownloader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3DocumentDownloaderTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private ZipService zipService;

    @InjectMocks
    private S3DocumentDownloader s3DocumentDownloader;

    private static final String BUCKET_NAME = "test-bucket";

    private ResponseInputStream<GetObjectResponse> fakeStream(String content) {
        return new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream(content.getBytes())));
    }

    @Test
    @DisplayName("downloadToZipV2_dueFile_scaricaEntrambeSenzaEccezioni")
    void downloadToZipV2_dueFile_scaricaEntrambeSenzaEccezioni() throws Exception {
        Set<String> fileNames = new LinkedHashSet<>();
        fileNames.add("doc1.txt");
        fileNames.add("doc2.txt");
        ZipInfo zipInfo = mock(ZipInfo.class);

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(fakeStream("contenuto1"), fakeStream("contenuto2"));

        s3DocumentDownloader.downloadToZipV2(BUCKET_NAME, fileNames, zipInfo);

        verify(s3Client, times(2)).getObject(any(GetObjectRequest.class));
        verify(zipService, times(2)).addEntryWithContent(eq(zipInfo), anyString(), anyString());
    }

    @Test
    @DisplayName("downloadToZipV2_setVuoto_nessunaScaricato")
    void downloadToZipV2_setVuoto_nessunaScaricato() throws Exception {
        Set<String> fileNames = Set.of();
        ZipInfo zipInfo = mock(ZipInfo.class);

        s3DocumentDownloader.downloadToZipV2(BUCKET_NAME, fileNames, zipInfo);

        verify(s3Client, never()).getObject(any(GetObjectRequest.class));
        verify(zipService, never()).addEntryWithContent(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("downloadToZipV2_erroreS3SuPrimoFile_continuaSuFileSuccessivo")
    void downloadToZipV2_erroreS3SuPrimoFile_continuaSuFileSuccessivo() throws Exception {
        Set<String> fileNames = new LinkedHashSet<>();
        fileNames.add("errore.txt");
        fileNames.add("ok.txt");
        ZipInfo zipInfo = mock(ZipInfo.class);

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(mock(S3Exception.class))
                .thenReturn(fakeStream("contenuto ok"));

        s3DocumentDownloader.downloadToZipV2(BUCKET_NAME, fileNames, zipInfo);

        verify(s3Client, times(2)).getObject(any(GetObjectRequest.class));
        verify(zipService, times(1)).addEntryWithContent(eq(zipInfo), eq("ok.txt"), anyString());
    }
}
