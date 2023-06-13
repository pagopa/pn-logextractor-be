package it.gov.pagopa.logextractor.util.external.s3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import it.gov.pagopa.logextractor.service.ZipInfo;
import it.gov.pagopa.logextractor.service.ZipService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3DocumentDownloader{
	
	@Autowired
	private AmazonS3 amazonS3Client;
	@Autowired
	private ZipService zipService;

	public void downloadToZip(String bucketName, Set<String> fileNames, ZipInfo zipInfo) {
		log.info("Starting download {} documents from {}",fileNames.size(), bucketName);
		for(String name: fileNames) {
			try {
				log.info("Retrieving document {} from s3 bucket {}", name, bucketName);
				long performanceMillis = System.currentTimeMillis();
				S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucketName, name));
				InputStream objectData = object.getObjectContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(objectData));
				String line = "";
				StringBuilder content = new StringBuilder();
				while((line = br.readLine()) != null) {
					content.append(line);
				}
				objectData.close();
				zipService.addEntryWithContent(zipInfo, name, content.toString());
				log.info("document {} retrieved in {} ms",name, System.currentTimeMillis() - performanceMillis);
			}catch (Exception err) {
				log.error("Error downloading document {} from S3 bucket {}",  name, bucketName, err);
			}
		}
	}
	
}
