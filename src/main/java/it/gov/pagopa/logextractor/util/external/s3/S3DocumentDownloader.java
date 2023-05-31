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
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FileInfo) {
				FileInfo fi =((FileInfo)obj); 
				return fi.name ==null && this.name==null? true : fi.name.equals(this.name);
			}else {
				return false;
			}
		}
		
		
	}

	private List<FileInfo> files;
	private String bucketName;
	private AmazonS3 amazonS3Client;
	
	public S3DocumentDownloader(AmazonS3 amazonS3Client, String bucket) {
		this.amazonS3Client = amazonS3Client;
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
			if (files.contains(fileInfo)) {
				log.info("Skipped S3 request for file {}", name);
			}else {
				try {
	
					log.info("Retrieving {} from s3 bucket {}", name, bucketName);
					long performanceMillis = System.currentTimeMillis();
					S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucketName, name));
					InputStream objectData = object.getObjectContent();
					BufferedReader br = new BufferedReader(new InputStreamReader(objectData));
					String line = "";
					while((line = br.readLine()) != null) {
						fileInfo.content.append(line);
					}
					objectData.close();
					log.info("SAML assertion from s3 bucket retrieved in {} ms",
							System.currentTimeMillis() - performanceMillis);
				}catch (Exception err) {
					log.error("Error downloading document {} from S3 bucket", name, err);
				}
				this.files.add(fileInfo);
			}
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
