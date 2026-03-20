package it.gov.pagopa.logextractor;

import it.gov.pagopa.logextractor.util.external.s3.S3ClientService;
import it.gov.pagopa.logextractor.util.external.s3.S3DocumentUploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ClientServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3DocumentUploader s3DocumentUploader;

    @InjectMocks
    private S3ClientService s3ClientService;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String KEY = "test/key.zip";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3ClientService, "bucketName", BUCKET_NAME);
    }

    @Test
    @DisplayName("downloadUrlV2_oggettoEsistente_restituisceUrlPresigned")
    void downloadUrlV2_oggettoEsistente_restituisceUrlPresigned() throws Exception {
        URL fakeUrl = new URL("https://s3.example.com/test-bucket/test/key.zip?X-Amz-Signature=abc");
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(fakeUrl);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        ResponseInputStream<GetObjectResponse> fakeStream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream(new byte[0])));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(fakeStream);

        String result = s3ClientService.downloadUrlV2(KEY);

        assertThat(result).isNotEqualTo("notready");
        assertThat(result).contains("https://");
    }

    @Test
    @DisplayName("downloadUrlV2_oggettoAssente_restituisceNotready")
    void downloadUrlV2_oggettoAssente_restituisceNotready() {
        S3Exception noSuchKey = mock(S3Exception.class);
        when(noSuchKey.awsErrorDetails()).thenReturn(AwsErrorDetails.builder().errorCode("NoSuchKey").build());
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(noSuchKey);

        String result = s3ClientService.downloadUrlV2(KEY);

        assertThat(result).isEqualTo("notready");
    }

    @Test
    @DisplayName("getObjectV2_chiaveEsistente_restituisceResponseInputStream")
    void getObjectV2_chiaveEsistente_restituisceResponseInputStream() {
        ResponseInputStream<GetObjectResponse> fakeStream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream("content".getBytes())));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(fakeStream);

        ResponseInputStream<GetObjectResponse> result = s3ClientService.getObjectV2(KEY);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getObjectV2_noSuchKey_restituisceNull")
    void getObjectV2_noSuchKey_restituisceNull() {
        S3Exception noSuchKey = mock(S3Exception.class);
        when(noSuchKey.awsErrorDetails()).thenReturn(AwsErrorDetails.builder().errorCode("NoSuchKey").build());
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(noSuchKey);

        ResponseInputStream<GetObjectResponse> result = s3ClientService.getObjectV2(KEY);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getObjectV2_accessDenied_rilanciaS3Exception")
    void getObjectV2_accessDenied_rilanciaS3Exception() {
        S3Exception accessDenied = mock(S3Exception.class);
        AwsErrorDetails details = AwsErrorDetails.builder().errorCode("AccessDenied").build();
        when(accessDenied.awsErrorDetails()).thenReturn(details);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(accessDenied);

        assertThatThrownBy(() -> s3ClientService.getObjectV2(KEY))
                .isInstanceOf(S3Exception.class);
    }
}
