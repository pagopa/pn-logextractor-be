package it.gov.pagopa.logextractor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AWSConfig {

	@Value("${external.s3.saml.assertion.region:eu-south-1}")
	private String bucketRegion;

	@Bean
	S3Client s3ClientV2() {
		return S3Client.builder()
				.region(Region.of(bucketRegion))
				.build();
	}

	@Bean
	S3Presigner s3PresignerV2() {
		return S3Presigner.builder()
				.region(Region.of(bucketRegion))
				.build();
	}
}
