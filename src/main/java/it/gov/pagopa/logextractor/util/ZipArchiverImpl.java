package it.gov.pagopa.logextractor.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public class ZipArchiverImpl implements IArchiver<EnhancedZipOutputStream> {

    private final String password;

    public ZipArchiverImpl( String password ) {
        this.password = password;
    }

    @Override
    public EnhancedZipOutputStream createArchiveStream(OutputStream outStream) throws IOException {
        if( this.password != null ) {
            return new EnhancedZipOutputStream(outStream, password.toCharArray() );
        }
        else {
            return new EnhancedZipOutputStream(outStream );
        }
    }

    @Override
    public void createNewArchiveTextEntry(EnhancedZipOutputStream archiveStream, String name) throws IOException {
        createNewArchiveBinaryEntry( archiveStream, name );
    }

    @Override
    public void createNewArchiveBinaryEntry(EnhancedZipOutputStream archiveStream, String name) throws IOException {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip( name );
        zipParameters.setCompressionLevel( CompressionLevel.HIGHER );

        if( this.password != null ) {
            zipParameters.setEncryptFiles( true );
            zipParameters.setEncryptionMethod( EncryptionMethod.AES );
        }

        archiveStream.putNextEntry( zipParameters );
    }

    @Override
    public void writeSomeBinaryData(EnhancedZipOutputStream archiveStream, byte[] data) throws IOException {
        archiveStream.write( data );
    }

    @Override
    public void writeSomeTextLines(EnhancedZipOutputStream archiveStream, List<String> text) throws IOException {
        byte[] data = String.join("\n", text).getBytes(StandardCharsets.UTF_8);

        writeSomeBinaryData( archiveStream, data );
    }

    @Override
    public void closeArchiveEntry(EnhancedZipOutputStream archiveStream) throws IOException {
        archiveStream.flush();
        archiveStream.closeEntry();
    }

    @Override
    public void flushArchive(EnhancedZipOutputStream archiveStream) throws IOException {
        archiveStream.flush();
    }
}
