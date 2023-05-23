package it.gov.pagopa.logextractor.service;

import java.io.File;

import it.gov.pagopa.logextractor.util.ZipArchiverImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;

@Data
@AllArgsConstructor
public class ZipInfo {
	String password;
	ZipOutputStream zos;
	ZipArchiverImpl zip;

	//TODO: rimuovere se confermata versione con stream
	private File tmpFile;
	private String key;

}
