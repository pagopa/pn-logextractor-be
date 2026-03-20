package it.gov.pagopa.logextractor;

import it.gov.pagopa.logextractor.exception.CustomException;
import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.external.s3.S3DocumentUploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import software.amazon.awssdk.core.sync.RequestBody;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3DocumentUploaderTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private FileUtilities fileutils;

    @InjectMocks
    private S3DocumentUploader s3DocumentUploader;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String KEY = "test/key.zip";

    @Test
    @DisplayName("uploadV3_inputStreamValido_completaMultipartUpload")
    void uploadV3_inputStreamValido_completaMultipartUpload() {
        when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-id-1").build());
        when(s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
                .thenReturn(UploadPartResponse.builder().eTag("etag-1").build());
        when(s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
                .thenReturn(CompleteMultipartUploadResponse.builder().build());

        InputStream is = new ByteArrayInputStream("hello world content for upload".getBytes());

        s3DocumentUploader.uploadV3(is, BUCKET_NAME, KEY);

        verify(s3Client).createMultipartUpload(any(CreateMultipartUploadRequest.class));
        verify(s3Client, atLeastOnce()).uploadPart(any(UploadPartRequest.class), any(RequestBody.class));
        verify(s3Client).completeMultipartUpload(any(CompleteMultipartUploadRequest.class));
    }

    @Test
    @DisplayName("uploadV3_erroreS3SuUploadPart_lanciaCustomException")
    void uploadV3_erroreS3SuUploadPart_lanciaCustomException() {
        when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-id-2").build());
        when(s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
                .thenThrow(mock(S3Exception.class));

        InputStream is = new ByteArrayInputStream("data".getBytes());

        assertThatThrownBy(() -> s3DocumentUploader.uploadV3(is, BUCKET_NAME, KEY))
                .isInstanceOf(CustomException.class);
    }
}
