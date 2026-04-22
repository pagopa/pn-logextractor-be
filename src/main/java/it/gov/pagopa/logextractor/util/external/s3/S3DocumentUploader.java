package it.gov.pagopa.logextractor.util.external.s3;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import it.gov.pagopa.logextractor.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

@Slf4j
@Service
public class S3DocumentUploader {

	private final S3Client s3ClientV2;

	public S3DocumentUploader(S3Client s3ClientV2) {
		this.s3ClientV2 = s3ClientV2;
	}

	@Async
	public void uploadV3(InputStream is, String bucketName, String key) {
		final int BUFFER_SIZE = 1024 * 1024 * 5;
		String uploadId = null;
		try {
			CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
					.bucket(bucketName)
					.key(key)
					.build();
			CreateMultipartUploadResponse createResponse = s3ClientV2.createMultipartUpload(createRequest);
			uploadId = createResponse.uploadId();

			List<CompletedPart> completedParts = new ArrayList<>();
			BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
			byte[] buffer = new byte[BUFFER_SIZE];
			int partNumber = 1;
			boolean finished = false;

			while (!finished) {
				int totalRead = 0;
				boolean reachedEOF = false;
				int readSize;
				while (totalRead < BUFFER_SIZE) {
					readSize = bis.read(buffer, totalRead, BUFFER_SIZE - totalRead);
					if (readSize == -1) {
						reachedEOF = true;
						break;
					}
					totalRead += readSize;
				}
				if (totalRead == 0) {
					abortMultipartUpload(bucketName, key, uploadId);
					uploadId = null;
					break;
				}
				finished = reachedEOF;

				software.amazon.awssdk.services.s3.model.UploadPartRequest uploadPartRequest =
						software.amazon.awssdk.services.s3.model.UploadPartRequest.builder()
								.bucket(bucketName)
								.key(key)
								.uploadId(uploadId)
								.partNumber(partNumber)
								.contentLength((long) totalRead)
								.build();
				software.amazon.awssdk.services.s3.model.UploadPartResponse uploadPartResponse = s3ClientV2.uploadPart(
						uploadPartRequest, RequestBody.fromBytes(Arrays.copyOf(buffer, totalRead)));
				completedParts.add(CompletedPart.builder()
						.partNumber(partNumber)
						.eTag(uploadPartResponse.eTag())
						.build());
				log.info("Uploaded part (v3) {}, size {} to bucket {} for key {}", partNumber, totalRead, bucketName, key);
				partNumber++;
			}

			if (uploadId != null) {
				software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest completeRequest =
						software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest.builder()
								.bucket(bucketName)
								.key(key)
								.uploadId(uploadId)
								.multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
								.build();
				s3ClientV2.completeMultipartUpload(completeRequest);
				log.info("Upload (v3) to bucket {} for key {} completed!", bucketName, key);
			}
		} catch (Exception err) {
			log.error("Error in uploadV3 to bucket", err);
			if (uploadId != null) {
				abortMultipartUpload(bucketName, key, uploadId);
			}
			CustomException customException = new CustomException(err.getMessage());
			customException.initCause(err);
			throw customException;
		}
	}

	private void abortMultipartUpload(String bucketName, String key, String uploadId) {
		try {
			s3ClientV2.abortMultipartUpload(AbortMultipartUploadRequest.builder()
					.bucket(bucketName)
					.key(key)
					.uploadId(uploadId)
					.build());
			log.info("Aborted multipart upload for key {} in bucket {}", key, bucketName);
		} catch (Exception abortErr) {
			log.error("Failed to abort multipart upload for key {} in bucket {}", key, bucketName, abortErr);
		}
	}

}
