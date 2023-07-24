package it.gov.pagopa.logextractor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.s3.AmazonS3;

@Configuration
public class AWSConfig {


	@Bean
	AmazonS3 amazonS3Client (S3ClientBuilder s3ClientBuilder) {
		
		return s3ClientBuilder.amazonS3Client();
	}
}
