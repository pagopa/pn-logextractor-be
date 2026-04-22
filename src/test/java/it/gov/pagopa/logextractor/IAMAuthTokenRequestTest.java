package it.gov.pagopa.logextractor;

import it.gov.pagopa.logextractor.config.IAMAuthTokenRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IAMAuthTokenRequestTest {

    @Mock
    private AwsCredentialsProvider credentialsProvider;

    @Test
    @DisplayName("toSignedRequestUri_credentialsProviderV2_restituisceUriConXAmzSignature")
    void toSignedRequestUri_credentialsProviderV2_restituisceUriConXAmzSignature() throws URISyntaxException {
        when(credentialsProvider.resolveCredentials())
                .thenReturn(AwsBasicCredentials.create("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"));

        IAMAuthTokenRequest request = new IAMAuthTokenRequest("test-user", "my-cache", "eu-south-1", false);

        String uri = request.toSignedRequestUri(credentialsProvider);

        assertThat(uri).contains("X-Amz-Signature");
    }

    @Test
    @DisplayName("toSignedRequestUri_credentialsProviderV2Serverless_restituisceUriConResourceType")
    void toSignedRequestUri_credentialsProviderV2Serverless_restituisceUriConResourceType() throws URISyntaxException {
        when(credentialsProvider.resolveCredentials())
                .thenReturn(AwsBasicCredentials.create("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"));

        IAMAuthTokenRequest request = new IAMAuthTokenRequest("test-user", "my-cache", "eu-south-1", true);

        String uri = request.toSignedRequestUri(credentialsProvider);

        assertThat(uri).contains("ResourceType");
    }
}
