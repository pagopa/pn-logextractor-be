package it.gov.pagopa.logextractor.util.external.filestorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import it.gov.pagopa.logextractor.util.external.IStorageService;

@Profile("dev2")
@Service
public class FileClientService implements IStorageService{
	@Value("${localstorage.basepath:c:\\tmp\\}")
	private String basePath;

	@Override
	public OutputStream uploadStreamV2(String keyName) {
		try {
			return new CustomFileOutputStream(basePath+keyName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Object getObject(String key) {
		return new File (basePath+key);
	}

	@Override
	public String downloadUrl(String key) {
		return basePath+key;
	}

}
