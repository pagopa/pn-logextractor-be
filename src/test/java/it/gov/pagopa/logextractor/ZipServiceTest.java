package it.gov.pagopa.logextractor;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.gov.pagopa.logextractor.service.ZipInfo;
import it.gov.pagopa.logextractor.service.ZipService;

@SpringBootTest
class ZipServiceTest {

	@Autowired ZipService zipService;
	
	@Test
	void testZip() throws Exception{
		File tmp = File.createTempFile("testZipService", null);
		FileOutputStream fos = new FileOutputStream(tmp);
		ZipInfo zipInfo = zipService.createZip("key", "pass", fos);
		
		zipService.addEntry(zipInfo, "esempio.txt", "Questo è un testo di prova".getBytes());
		zipService.close(zipInfo);
		
		fos.flush();
		fos.close();
		Assertions.assertTrue(tmp.length()>0);
	}
}
