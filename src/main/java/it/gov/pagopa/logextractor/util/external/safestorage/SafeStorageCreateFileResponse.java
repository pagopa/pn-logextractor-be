package it.gov.pagopa.logextractor.util.external.safestorage;

import lombok.Data;

@Data
public class SafeStorageCreateFileResponse {

	String uploadUrl;
	String key;
	String secret;
}
