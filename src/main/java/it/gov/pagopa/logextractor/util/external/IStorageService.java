package it.gov.pagopa.logextractor.util.external;

import java.io.OutputStream;

public interface IStorageService {

	OutputStream uploadStreamV2(String keyName);

	Object getObject(String key);

	String downloadUrl(String key);

}
