package it.gov.pagopa.logextractor.util;

import java.io.IOException;
import java.io.OutputStream;

import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;

/**
 * Extends ZipOutputStream, avoid writing empty ZipEntries
 * @author ivanr
 *
 */
public class EnhancedZipOutputStream extends ZipOutputStream{

	private boolean headerCreated = false;
	private ZipParameters pendingZipParameters;
	
	public EnhancedZipOutputStream(OutputStream outputStream) throws IOException {
		super(outputStream);
	}

	public EnhancedZipOutputStream(OutputStream outStream, char[] charArray) throws IOException {
		super(outStream, charArray);
	}

	@Override
	public void putNextEntry(ZipParameters zipParameters) throws IOException {
		this.pendingZipParameters = zipParameters;
	}
	
	private void checkEntryExists() throws IOException {
		if (!headerCreated) {
			super.putNextEntry(pendingZipParameters);
			headerCreated = true;
		}
	}

	@Override
	public void write(int b) throws IOException {
		checkEntryExists();
		super.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		checkEntryExists();
		super.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		checkEntryExists();
		super.write(b, off, len);
	}

	@Override
	public FileHeader closeEntry() throws IOException {
		if (headerCreated) {
			headerCreated = false;
			pendingZipParameters = null;
			return super.closeEntry();
		} else {
			return null;
		}
	}
}
