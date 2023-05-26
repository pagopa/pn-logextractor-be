package it.gov.pagopa.logextractor.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.gov.pagopa.logextractor.util.FileUtilities;
import it.gov.pagopa.logextractor.util.PasswordFactory;
import it.gov.pagopa.logextractor.util.ZipArchiverImpl;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import it.gov.pagopa.logextractor.util.external.s3.S3ClientService;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;

@Service
public class ZipService {

	@Autowired
	S3ClientService s3ClientService;
	
	@Autowired
	FileUtilities fileUtilities;
	
	
	public ZipInfo createZip(String key, String password, OutputStream targetStream) {
		ZipArchiverImpl zip = new ZipArchiverImpl(password);
		
		ZipOutputStream zos=null;
		File tmpFile=null;
		try {
			//TODO: rivedere a fine refactory
//			zos = zip.createArchiveStream(s3ClientService.openBucket(fileName));
			if (targetStream != null) {
				zos = zip.createArchiveStream(targetStream);
			}else {
				tmpFile = fileUtilities.getFileWithRandomName(key, ".zip");
				zos = zip.createArchiveStream(new FileOutputStream(tmpFile));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new ZipInfo(password, zos, zip, tmpFile, key);
	}
	
	public void addEntry(ZipInfo zipInfo, String name) throws IOException {
		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setFileNameInZip(name);
		zipParameters.setCompressionLevel(CompressionLevel.NO_COMPRESSION);
		if( zipInfo.getPassword() != null ) {
	        zipParameters.setEncryptFiles( true );
	        zipParameters.setEncryptionMethod( EncryptionMethod.AES );
		}

		zipInfo.getZos().putNextEntry(zipParameters);
	}
	
	public void addEntry(ZipInfo zipInfo,String name, String content) throws IOException{
		addEntry(zipInfo, name);
		OutputStreamWriter ow = new OutputStreamWriter(zipInfo.getZos());
		ow.write(content);
		ow.flush();
		closeEntry(zipInfo);		
	}
	public void addEntry(ZipInfo zipInfo,String name, byte[] content) throws IOException{
		addEntry(zipInfo, name);
		zipInfo.getZos().write(content);
		closeEntry(zipInfo);		
	}
	
	
	public void closeEntry(ZipInfo zipInfo) throws IOException {
		zipInfo.getZos().closeEntry();
	}
	
	public void close(ZipInfo zipInfo) throws IOException{
		zipInfo.getZos().flush();
		zipInfo.getZos().close();
		//Per la versione che carica alla fine
		//s3ClientService.uploadFile(key, tmpFile);
		if (zipInfo.getTmpFile()!=null) zipInfo.getTmpFile().delete();
	}
}
