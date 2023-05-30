package it.gov.pagopa.logextractor.util.external.s3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3DocumentUploader {
	@Autowired
	AmazonS3 s3Client;
	
	@Async
	public void upload(PutObjectRequest por) {
		try {
        	s3Client.putObject(por);

        	log.info("Upload to bucket completed!");
        } catch(Exception err) {
            log.error("Error in thread upload to bucket", err);
        }
	}
}
