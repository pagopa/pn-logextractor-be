package it.gov.pagopa.logextractor.util.external.opensearch;

import java.io.BufferedInputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import it.gov.pagopa.logextractor.service.ThreadLocalOutputStreamService;
import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3DocumentDownloader implements OpenSearchApiObserver {

	private ThreadLocalOutputStreamService threadLocalService;
	private String downloadFileUrl;
	
	public S3DocumentDownloader(String downloadFileUrl, ThreadLocalOutputStreamService threadLocalService) {
		this.downloadFileUrl = downloadFileUrl;
		this.threadLocalService = threadLocalService;
	}

	@Override
	public void notify(String document, int numDoc) {
		if (numDoc!=1) return;
		
		JsonUtilities jsonUtils = new JsonUtilities();
		String date = jsonUtils.getValue(document, "@timestamp");
		if(StringUtils.isNotBlank(date)) {
			String name = String.format("%s-%s", jsonUtils.getValue(document, "jti"),
					LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate().toString());

			try {
				threadLocalService.addEntry(name+GenericConstants.JSON_EXTENSION);
				String downloadUrl = String.format(downloadFileUrl, name);
				log.info("Retrieving SAML assertion from s3 bucket... ");
				long performanceMillis = System.currentTimeMillis();
				BufferedInputStream in = new BufferedInputStream(new URL(downloadUrl).openStream());
			    byte dataBuffer[] = new byte[1024];
			    int bytesRead;
			    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
			    	threadLocalService.get().write(dataBuffer, 0, bytesRead);
			    }
			    threadLocalService.closeEntry();
				log.info("SAML assertion from s3 bucket retrieved in {} ms, constructing service response...",
						System.currentTimeMillis() - performanceMillis);
			}catch (Exception err) {
				log.error("Error downloading documento from S3 bucket", err);
			}
		}
	}
}
