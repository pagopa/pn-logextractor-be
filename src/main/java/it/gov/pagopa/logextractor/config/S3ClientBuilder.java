package it.gov.pagopa.logextractor.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class S3ClientBuilder {
	@Value("${external.s3.saml.assertion.awsprofile:}")
	String awsProfile;

	@Value("${external.s3.saml.assertion.region:eu-south-1}")
	String bucketRegion;

	@Value("${external.s3.http.max-connections:3}")
	Integer httpMaxConnections;
	@Value("${external.s3.http.max-retry:10}")
	Integer httpMaxRetry;

	public AmazonS3 amazonS3Client() {
		log.info("Initializing S3Cient......");
		ClientConfiguration clientConfiguration = new ClientConfiguration().withMaxErrorRetry(httpMaxRetry).withMaxConnections(httpMaxConnections);

		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
		if (StringUtils.isNotBlank(awsProfile)) {
			log.info("Initializing S3Cient with profile {} and region {}",awsProfile, bucketRegion);
			builder = builder.withCredentials(new ProfileCredentialsProvider(awsProfile));
		} else {
			log.info("Initializing S3Cient with no profile and region {}", bucketRegion);
		}
		
		return builder.withRegion(bucketRegion).withClientConfiguration(clientConfiguration).build();
	}
}
