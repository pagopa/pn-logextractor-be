package it.gov.pagopa.logextractor;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import it.gov.pagopa.logextractor.config.AWSConfig;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import it.gov.pagopa.logextractor.util.external.s3.S3ApiHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class S3ApiHandlerTest {

  @Mock
  @Qualifier("amazonS3Client")
  AmazonS3 s3Client;

  @InjectMocks
  S3ApiHandler s3ApiHandler = new S3ApiHandler();

  @Test
  void testGetFile_whenInputDataIsValid_thenReturnsFile() {
    Assertions.assertThrows(LogExtractorException.class,
        () -> s3ApiHandler.getFile("test", GenericConstants.JSON_EXTENSION));
  }
}
