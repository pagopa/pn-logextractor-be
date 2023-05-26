package it.gov.pagopa.logextractor.util.external.s3;

import java.io.File;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
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

	@Value("${external.s3.saml.assertion.awsprofile:}")
	String awsProfile;
	
	@Value("${external.s3.saml.assertion.region:eu-south-1}")
	String bucketRegion;
	
	private static String CONTENT_TYPE = "application/zip";
	


	public void uploadFile(String keyName, File file) {
		try {
			log.info("Starting upload to bucket .....");
			AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
			if (StringUtils.isNotBlank(awsProfile)) {
				builder = builder.withCredentials(new ProfileCredentialsProvider(awsProfile));
			}
			
			
			AmazonS3 s3Client = builder.withRegion(bucketRegion).build();

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
		    
			AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
			if (StringUtils.isNotBlank(awsProfile)) {
				builder = builder.withCredentials(new ProfileCredentialsProvider(awsProfile));
			}
		    
			AmazonS3 s3Client = builder.withRegion(bucketRegion)
					.build();
			ObjectMetadata metadata = new ObjectMetadata();
			PutObjectRequest por = new PutObjectRequest(bucketName,
	                keyName,
	                in,
	                metadata);
			Thread thread1 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		            try {
		            	s3Client.putObject(por);

		            	log.info("Upload to bucket completed!");
		            } catch(Exception err) {
		                log.error("Error in thread upload to bucket", err);
		            }                   
		        }
		    });
			thread1.start();
			
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
				AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
				if (StringUtils.isNotBlank(awsProfile)) {
					builder = builder.withCredentials(new ProfileCredentialsProvider(awsProfile));
				}
				
				AmazonS3 s3Client = builder.withRegion(bucketRegion).build();
	
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
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
		if (StringUtils.isNotBlank(awsProfile)) {
			builder = builder.withCredentials(new ProfileCredentialsProvider(awsProfile));
		}
		
		AmazonS3 s3Client = builder.withRegion(bucketRegion).build();
		
		log.info("Retrieving SAML assertion from s3 bucket... ");
		try {
			object = s3Client.getObject(new GetObjectRequest(bucketName, key));
		}catch(AmazonS3Exception err) {
			if (err.getErrorCode().equals("NoSuchKey")){
				//Do nothing
				log.debug("download url not ready for key {}",key);
			}else {
				if (err.getErrorCode().equals("AccessDenied")) {
					log.error("Access denied for key: {} at bucket: {} with profile: {} in region: {}",key, bucketName, awsProfile,bucketRegion);
				}
				throw err;
			}
			
		}
		return object;
	}

}
