package it.gov.pagopa.logextractor.util.external.opensearch;

import java.io.*;
import java.util.List;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
/**
 * 
 * @author ivanr
 *
 *Classe mock per evitare richiesta a OpenSearch: recupera un risultato pre-salvato
 */
@Profile("mockedOS")
@Component
public class MockedOpenSearchApiHandler extends OpenSearchApiHandler {

	public MockedOpenSearchApiHandler(RestTemplate client, String opensearchSearchUrl,
			String opensearchSearchFollowupUrl, String opensearchUsername, String opensearchPassword,
			List<OpenSearchApiObserver> observers, int docCounter) {
		super(client, opensearchSearchUrl, opensearchSearchFollowupUrl, opensearchUsername, opensearchPassword, observers,
				docCounter);
	}

	@Override
	@SneakyThrows({FileNotFoundException.class, IOException.class})
	public int getAnonymizedLogsByIun(String iun, String dateFrom, String dateTo, OutputStream out) {
		InputStream is = new FileInputStream("c:\\tmp\\test-deanonim.txt");
		is.transferTo(out);
		is.close();
		return 1;
	}

}
