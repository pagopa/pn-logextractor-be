package it.gov.pagopa.logextractor.util.external.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import it.gov.pagopa.logextractor.exception.LogExtractorException;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class S3ApiHandler {

  @Value("${external.s3.saml.assertion.bucketName}")
  String bucketName;
  @Autowired
  @Qualifier("amazonS3Client")
  AmazonS3 s3Client;

  public File getFile(String fileName, String extension)
      throws IOException, LogExtractorException {
    log.info("Getting assertion file... file={}", fileName + extension);
    S3Object s3object = s3Client.getObject(bucketName, fileName + extension);
    if (s3object == null) {
      throw new LogExtractorException("No assertion file in S3 bucket");
    }
    S3ObjectInputStream inputStream = s3object.getObjectContent();
    File samlAssertion = new File(GenericConstants.EXPORT_FOLDER + fileName + extension);
    FileUtils.copyInputStreamToFile(inputStream, samlAssertion);
    return samlAssertion;
  }
}
