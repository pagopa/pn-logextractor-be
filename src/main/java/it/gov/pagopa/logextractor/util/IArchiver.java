package it.gov.pagopa.logextractor.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface IArchiver<A> {

    A createArchiveStream( OutputStream outStream ) throws IOException;

    void createNewArchiveTextEntry( A archiveStream, String name ) throws IOException;

    void createNewArchiveBinaryEntry( A archiveStream, String name ) throws IOException;

    void writeSomeTextLines( A archiveStream, List<String> text ) throws IOException;

    void writeSomeBinaryData( A archiveStream, byte[] data ) throws IOException;

    void closeArchiveEntry( A archiveStream ) throws IOException;

    void flushArchive( A archiveStream ) throws IOException;
}
