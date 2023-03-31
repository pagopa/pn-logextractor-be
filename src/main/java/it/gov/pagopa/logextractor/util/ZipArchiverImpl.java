package it.gov.pagopa.logextractor.util;

import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ZipArchiverImpl implements IArchiver<ZipOutputStream> {

    private final String password;

    public ZipArchiverImpl( String password ) {
        this.password = password;
    }

    @Override
    public ZipOutputStream createArchiveStream(OutputStream outStream) throws IOException {
        if( this.password != null ) {
            return new ZipOutputStream(outStream, password.toCharArray() );
        }
        else {
            return new ZipOutputStream(outStream );
        }
    }

    @Override
    public void createNewArchiveTextEntry(ZipOutputStream archiveStream, String name) throws IOException {
        createNewArchiveBinaryEntry( archiveStream, name );
    }

    @Override
    public void createNewArchiveBinaryEntry(ZipOutputStream archiveStream, String name) throws IOException {
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
    public void writeSomeBinaryData(ZipOutputStream archiveStream, byte[] data) throws IOException {
        archiveStream.write( data );
    }

    @Override
    public void writeSomeTextLines(ZipOutputStream archiveStream, List<String> text) throws IOException {
        byte[] data = String.join("\n", text).getBytes(StandardCharsets.UTF_8);

        writeSomeBinaryData( archiveStream, data );
    }

    @Override
    public void closeArchiveEntry(ZipOutputStream archiveStream) throws IOException {
        archiveStream.flush();
        archiveStream.closeEntry();
    }

    @Override
    public void flushArchive(ZipOutputStream archiveStream) throws IOException {
        archiveStream.flush();
    }
}
