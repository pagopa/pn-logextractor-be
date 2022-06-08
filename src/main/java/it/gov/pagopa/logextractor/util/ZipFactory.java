package it.gov.pagopa.logextractor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.Base64Utils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;

/**
 * Factory class to manage the core operations for a zip archive
 */
public class ZipFactory {

	/**
	 * Create a new zip archive protected by password
	 * @param name the name of the new zip archive
	 * @param password the password to protect the new archive
	 * @return a new instance of a zip archive with the given name protected by the given password
	 * */
	public ZipFile createZipArchive(String name, String password) {
		return new ZipFile(Constants.EXPORT_FOLDER + name + "-" + 
							new RandomGenerator().generateRandomToken() + Constants.ZIP_EXTENSION, password.toCharArray());
	}
	
	/**
	 * Create new zip parameters to use when adding files to a zip archive
	 * @param encryptFiles set if the files should be encrypted or not
	 * @param compressionLevel the level of the compression that should be applied to the files
	 * @param encryptionMethod the encryption method
	 * @return a new instance of zip parameters
	 * */
	public ZipParameters createZipParameters(boolean encryptFiles, CompressionLevel compressionLevel, EncryptionMethod encryptionMethod) {
		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setEncryptFiles(encryptFiles);
		zipParameters.setCompressionLevel(compressionLevel);
		zipParameters.setEncryptionMethod(encryptionMethod);
		return zipParameters;
	}
	
	/**
	 * Add a file with zip parameters to a zip archive 
	 * @param archive the zip archive where to add the file
	 * @param parameters the parameters for the file
	 * @param file the file add
	 * @return the input zip with the addition of the file
	 * */
	public ZipFile addFile(ZipFile archive, ZipParameters parameters, File file) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		archive.addFile(file, parameters);
		archive.close();
		return archive;
	}
	
	/**
	 * Convert zip archive to byte array
	 * @param archive The zip archive to convert
	 * @return A byte array representation of the input zip archive
	 * @throws IOException in case of IO Error
	 * */
	public byte[] toByteArray(ZipFile archive) throws IOException {
		InputStream stream = new FileInputStream(archive.getFile());
		byte[] output = stream.readAllBytes();
		stream.close();
		return output;
	}
}