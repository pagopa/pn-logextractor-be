package it.gov.pagopa.logextractor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDownloadInfo {

	private String url;
	private Integer retryAfter;
}
