package it.gov.pagopa.logextractor;

import it.gov.pagopa.logextractor.config.PnLogExtractorConnectionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class PnLogExtractorConnectionFactoryTest {

    @Test
    @DisplayName("credentialsProvider_campoUsaSDKv2_tipoAwsCredentialsProvider")
    void credentialsProvider_campoUsaSDKv2_tipoAwsCredentialsProvider() throws NoSuchFieldException {
        Field field = PnLogExtractorConnectionFactory.class.getDeclaredField("credentialsProvider");

        assertThat(AwsCredentialsProvider.class)
                .isAssignableFrom(field.getType());
    }
}
