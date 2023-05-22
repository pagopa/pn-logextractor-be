package it.gov.pagopa.logextractor.rest;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.pn_logextractor_be.api.StatusApi;
import it.gov.pagopa.logextractor.util.external.s3.S3ClientService;
import it.gov.pagopa.logextractor.util.external.safestorage.SafeStorageClient;
import it.gov.pagopa.logextractor.util.external.safestorage.SafeStorageCreateFileResponse;

@RestController
public class HealthCheckController implements StatusApi {

	@Autowired
	SafeStorageClient safeStorageClient;
	
	@Autowired
	S3ClientService s3ClientService;
	
	@Override
	public ResponseEntity<Void> healthStatus() throws Exception{
		s3Stuff();
		return ResponseEntity.ok().build();
	}
	
	private void safeStuff() throws Exception{
		byte[] zip = IOUtils.toByteArray(new FileInputStream("c:\\tmp\\signedreq.zip"));
		SafeStorageCreateFileResponse createResp = safeStorageClient.createFle(zip);
		
		safeStorageClient.uploadToSafeStorage(createResp, zip);
		try {
			Thread.currentThread().sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String downloadUrl = safeStorageClient.getDownloadUrl(createResp.getKey());
		System.out.println("Download URL "+downloadUrl);
		
	}
	
	private void s3Stuff() {
		String key="prova"+ new Date().getTime()+".txt";
//		URL url = s3ClientService.signedUrlForUpload();
//		s3ClientService.upload(url, key);
		s3ClientService.signBucket(key);
		String url = s3ClientService.downloadUrl(key);
//		
		System.out.println("(signed) Download url: "+url);
	}
}

