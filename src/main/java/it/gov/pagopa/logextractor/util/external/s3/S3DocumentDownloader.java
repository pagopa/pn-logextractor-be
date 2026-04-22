package it.gov.pagopa.logextractor.util.external.s3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.stereotype.Service;

import it.gov.pagopa.logextractor.service.ZipInfo;
import it.gov.pagopa.logextractor.service.ZipService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@Service
public class S3DocumentDownloader {

	private final S3Client s3Client;
	private final ZipService zipService;

	public S3DocumentDownloader(S3Client s3Client, ZipService zipService) {
		this.s3Client = s3Client;
		this.zipService = zipService;
	}

	public void downloadToZipV2(String bucketName, Set<String> fileNames, ZipInfo zipInfo) {
		log.info("Starting download (v2) {} documents from {}", fileNames.size(), bucketName);
		for (String name : fileNames) {
			try {
				log.info("Retrieving document (v2) {} from s3 bucket {}", name, bucketName);
				long t0 = System.currentTimeMillis();
				software.amazon.awssdk.services.s3.model.GetObjectRequest request =
						software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
								.bucket(bucketName)
								.key(name)
								.build();
				try (ResponseInputStream<GetObjectResponse> objectData = s3Client.getObject(request);
					 BufferedReader br = new BufferedReader(new InputStreamReader(objectData, StandardCharsets.UTF_8))) {
					StringBuilder content = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) {
						content.append(line);
					}
					zipService.addEntryWithContent(zipInfo, name, content.toString());
				}
				log.info("document (v2) {} retrieved in {} ms", name, System.currentTimeMillis() - t0);
			} catch (Exception err) {
				log.error("Error downloading document (v2) {} from S3 bucket {}", name, bucketName, err);
			}
		}
	}

}
