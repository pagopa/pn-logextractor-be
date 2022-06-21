package it.gov.pagopa.logextractor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PnLogextractorBeApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PnLogextractorBeApplicationTests {

	@Test
	void main() {
		PnLogextractorBeApplication.main(new String[] {});
		Assertions.assertTrue(true);
	}
}
