package it.gov.pagopa.logextractor.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AWSConfig {
	@Value("${external.s3.saml.assertion.awsprofile:}")
	String awsProfile;

	@Value("${external.s3.saml.assertion.region:eu-south-1}")
	String bucketRegion;

	@Bean
	AmazonS3 amazonS3Client() {
		log.info("Initializing S3Cient...");

		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
		if (StringUtils.isNotBlank(awsProfile)) {
			log.info("Initializing S3Cient with profile {} and region {}",awsProfile, bucketRegion);
			builder = builder.withCredentials(new ProfileCredentialsProvider(awsProfile));
		} else {
			log.info("Initializing S3Cient with no profile and region {}", bucketRegion);
		}
		
		return builder.withRegion(bucketRegion).build();
	}
}
