package it.gov.pagopa.logextractor.service;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import it.gov.pagopa.logextractor.util.PasswordFactory;
import it.gov.pagopa.logextractor.util.ZipArchiverImpl;
import it.gov.pagopa.logextractor.util.constant.GenericConstants;
import it.gov.pagopa.logextractor.util.external.s3.S3ClientService;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;

@Component
public class ThreadLocalOutputStreamService {
	

	private static ThreadLocal<ZipInfo> local = new ThreadLocal<>();
	
	/*
	 * 		

	 */

	@Deprecated
	public void initialize(HttpServletResponse httpServletResponse, String attachmentName) throws IOException {
		PasswordFactory passwordFactory = new PasswordFactory();
		String password = passwordFactory.createPassword(1, 1, 1, GenericConstants.SPECIAL_CHARS, 1, 16);
		ZipArchiverImpl zip = new ZipArchiverImpl(password);
		httpServletResponse.addHeader("Access-Control-Expose-Headers", "password,content-disposition");
		httpServletResponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s.zip", attachmentName));
		httpServletResponse.addHeader("password", password);
		httpServletResponse.addHeader("Content-Type",MediaType.APPLICATION_OCTET_STREAM_VALUE);
		ZipOutputStream zos = zip.createArchiveStream(httpServletResponse.getOutputStream());
		local.set(new ZipInfo(password, zos, zip, null, null));
	}
	
	
	public ZipOutputStream get() {
		return local.get().getZos();
	}
	
	public void addEntry(String name) throws IOException {
		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setFileNameInZip(name);
		zipParameters.setCompressionLevel(CompressionLevel.NO_COMPRESSION);
		if( local.get().password != null ) {
	        zipParameters.setEncryptFiles( true );
	        zipParameters.setEncryptionMethod( EncryptionMethod.AES );
		}

		get().putNextEntry(zipParameters);
	}
	
	/**
	 * Add a new entry with given content then close the entry
	 * @param name
	 * @param content
	 * @throws IOException
	 */
	public void addEntry(String name, String content) throws IOException{
		addEntry(name);
		OutputStreamWriter ow = new OutputStreamWriter(this.get());
		ow.write(content);
		ow.flush();
		closeEntry();		
	}
	
	public void closeEntry() throws IOException {
		get().closeEntry();
	}
	
	public String getPassword() {
		return local.get().getPassword();
	}
	public void remove() {
		local.remove();
	}
}
