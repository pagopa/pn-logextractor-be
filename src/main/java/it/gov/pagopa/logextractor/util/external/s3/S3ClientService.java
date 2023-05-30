package it.gov.pagopa.logextractor.util.external.s3;

import java.io.File;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class S3ClientService {

	@Value("${bucket.name:logextractor-bucket}")
	String bucketName;

	@Autowired
	AmazonS3 s3Client;
	
	@Autowired
	S3DocumentUploader s3DocumentUploader;
	
	public void uploadFile(String keyName, File file) {
		try {
			log.info("Starting upload to bucket .....");

			PutObjectRequest por = new PutObjectRequest(bucketName,
	                keyName,
	                file);
			s3Client.putObject(por);
			
			log.info("File uploaded to bucket !");
		}catch(Exception err) {
			log.error("Error uploading file", err);
		}
        

		
	}
	
	
	public OutputStream uploadStream(String keyName) {
		try {
			log.info("Starting upload to bucket .....");
			PipedInputStream in = new PipedInputStream();
		    PipedOutputStream out = new PipedOutputStream(in);
		    
			ObjectMetadata metadata = new ObjectMetadata();
			PutObjectRequest por = new PutObjectRequest(bucketName,
	                keyName,
	                in,
	                metadata);
			
			s3DocumentUploader.upload(por);
			
			log.info("Opened upload stream to bucket !");
			return out;
		}catch(Exception err) {
			log.error("Error uploading file", err);
		}
		return null;
	}


	public String downloadUrl(String objectKey) {

		try {
			if (getObject(objectKey)!=null) {
				// Set the presigned URL to expire after one hour.
				java.util.Date expiration = new java.util.Date();
				long expTimeMillis = Instant.now().toEpochMilli();
				expTimeMillis += 1000 * 60 * 60;
				expiration.setTime(expTimeMillis);
	
				// Generate the presigned URL.
				log.info("Generating pre-signed URL for download.");
				GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
						objectKey).withMethod(com.amazonaws.HttpMethod.GET).withExpiration(expiration);
				URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
	
				log.info("Pre-Signed URL for download: " + url.toString());
				return url.toString();
			}
		} catch (Exception e) {
			log.error("Error getting downloadURL from S3", e);
		}
		return "notready";
	}

	public S3Object getObject(String key) {
		S3Object object = null;
		
		log.info("Retrieving SAML assertion from s3 bucket... ");
		try {
			object = s3Client.getObject(new GetObjectRequest(bucketName, key));
		}catch(AmazonS3Exception err) {
			if (err.getErrorCode().equals("NoSuchKey")){
				//Do nothing
				log.debug("download url not ready for key {}",key);
			}else {
				if (err.getErrorCode().equals("AccessDenied")) {
					log.error("Access denied for key: {} at bucket: {} in region: {}",key, bucketName ,s3Client.getRegionName()  );
				}
				throw err;
			}
			
		}
		return object;
	}

}
