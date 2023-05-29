package it.gov.pagopa.logextractor.util.external.s3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3DocumentDownloader implements OpenSearchApiObserver {
	
	private class FileInfo{
		StringBuffer content = new StringBuffer();
		String name;
	}

	private String profile;
	private List<FileInfo> files;
	private String region;
	private String bucketName;
	
	public S3DocumentDownloader(String profile, String region, String bucket) {
		this.profile = profile;
		this.region = region;
		this.bucketName = bucket;
		
		this.files = new ArrayList<>();
	}

	@Override
	public void notify(String document, int numDoc) {
//		if (numDoc != 1) return;
		
		JsonUtilities jsonUtils = new JsonUtilities();
		String date = jsonUtils.getValue(document, "@timestamp");
		String jti = jsonUtils.getValue(document, "jti");
		if(StringUtils.isNotBlank(date) && StringUtils.isNotBlank(jti)) {
			String name = String.format("%s-%s.json", jti,
					LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate().toString());

			FileInfo fileInfo = new FileInfo();
			fileInfo.name=name;
			try {
				AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
				if (StringUtils.isNotBlank(profile)) {
					builder = builder.withCredentials(new ProfileCredentialsProvider(profile));
				}
				AmazonS3 amazonS3Client = builder
					.withRegion(region)
//					.withCredentials(
//                        new AWSStaticCredentialsProvider(
//                            new BasicAWSCredentials(accessKey, accessSecret)))
					.build();

				log.info("Retrieving SAML assertion from s3 bucket... ");
				long performanceMillis = System.currentTimeMillis();
				S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucketName, name));
				InputStream objectData = object.getObjectContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(objectData));
				String line = "";
				while((line = br.readLine()) != null) {
					fileInfo.content.append(line);
				}
				objectData.close();
				log.info("SAML assertion from s3 bucket retrieved in {} ms, constructing service response...",
						System.currentTimeMillis() - performanceMillis);
			}catch (Exception err) {
				log.error("Error downloading document {} from S3 bucket", fileInfo.name, err);
			}
			this.files.add(fileInfo);
		}
	}
	
	public Map<String, String> getFiles(){
		Map<String,String> ret = new HashMap<>();
		
		for(FileInfo info:files) {
			ret.put(info.name, info.content.toString());
		}
		
		return ret;
	}
}
