package it.gov.pagopa.logextractor.util.external.s3;

import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import it.gov.pagopa.logextractor.util.external.IStorageService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Profile("!dev2")
@Service
@Slf4j
public class S3ClientService implements IStorageService {

	@Value("${bucket.name:logextractor-bucket}")
	String bucketName;

	private final S3DocumentUploader s3DocumentUploader;
	private final S3Client s3Client;
	private final S3Presigner s3Presigner;

	public S3ClientService(S3DocumentUploader s3DocumentUploader,
						   S3Client s3Client,
						   S3Presigner s3Presigner) {
		this.s3DocumentUploader = s3DocumentUploader;
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
	}

	@Override
	public OutputStream uploadStreamV2(String keyName) {
		try {
			log.info("Starting upload to bucket .....");
			PipedInputStream in = new PipedInputStream();
			PipedOutputStream out = new PipedOutputStream(in);
			s3DocumentUploader.uploadV3(in, bucketName, keyName);
			log.info("Opened upload stream to bucket !");
			return out;
		} catch (Exception err) {
			log.error("Error uploading file", err);
		}
		return null;
	}

	@Override
	public Object getObject(String key) {
		return getObjectV2(key);
	}

	@Override
	public String downloadUrl(String objectKey) {
		return downloadUrlV2(objectKey);
	}

	public String downloadUrlV2(String objectKey) {
		try {
			if (getObjectV2(objectKey) == null) {
				return "notready";
			}
			log.info("Generating pre-signed URL for download of key {}", objectKey);
			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(Duration.ofHours(1))
					.getObjectRequest(b -> b.bucket(bucketName).key(objectKey))
					.build();
			PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
			String url = presigned.url().toString();
			log.info("Pre-Signed URL for download: {}", url);
			return url;
		} catch (Exception e) {
			log.error("Error getting downloadURL from S3", e);
		}
		return "notready";
	}

	public ResponseInputStream<GetObjectResponse> getObjectV2(String key) {
		log.info("Retrieving object from s3 bucket with key {}", key);
		try {
			software.amazon.awssdk.services.s3.model.GetObjectRequest request =
					software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
							.bucket(bucketName)
							.key(key)
							.build();
			return s3Client.getObject(request);
		} catch (S3Exception err) {
			String errorCode = err.awsErrorDetails().errorCode();
			if ("NoSuchKey".equals(errorCode)) {
				log.debug("download url not ready for key {}", key);
				return null;
			}
			if ("AccessDenied".equals(errorCode)) {
				log.error("Access denied for key: {} at bucket: {}", key, bucketName);
			}
			throw err;
		}
	}

}
