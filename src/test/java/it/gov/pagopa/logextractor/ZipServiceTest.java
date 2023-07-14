package it.gov.pagopa.logextractor;

import java.io.FileOutputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.gov.pagopa.logextractor.service.ZipInfo;
import it.gov.pagopa.logextractor.service.ZipService;
import it.gov.pagopa.logextractor.util.RandomUtils;

@SpringBootTest
public class ZipServiceTest {

	@Autowired ZipService zipService;
	
	@Test
	public void testZip() throws Exception{
		FileOutputStream tmp = new FileOutputStream("c:\\tmp\\testZipService"+RandomUtils.generateRandomAlphaNumericString()+".zip");
		ZipInfo zipInfo = zipService.createZip("key", "pass", tmp);
		
		zipService.addEntry(zipInfo, "esempio.txt", "Questo è un testo di prova".getBytes());
		zipService.close(zipInfo);
		
		tmp.flush();
		tmp.close();
	}
}
