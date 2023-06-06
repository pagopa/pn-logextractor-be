package it.gov.pagopa.logextractor.util.external.s3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3DocumentUploader {
	@Autowired
	AmazonS3 s3Client;
	
	@Async
	public void upload(PutObjectRequest por) {
		try {
			TransferManager tm = TransferManagerBuilder.standard()
                    .withS3Client(s3Client)
                    .build();
			
        	Upload upload = tm.upload(por);

        	UploadResult result = upload.waitForUploadResult();
        	log.info("Upload to bucket completed! Version: {}", result.getVersionId());
        } catch(Exception err) {
            log.error("Error in thread upload to bucket", err);
        }
	}
}
