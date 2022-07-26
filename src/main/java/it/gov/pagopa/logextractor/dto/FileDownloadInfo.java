package it.gov.pagopa.logextractor.dto;

import lombok.Getter;

@Getter
public class FileDownloadInfo {

	private String url;
	private Integer retryAfter;
}
