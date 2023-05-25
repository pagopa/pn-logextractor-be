package it.gov.pagopa.logextractor.util.external.opensearch;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
	public int getAnonymizedLogsByIun(String iun, String dateFrom, String dateTo, OutputStream out) {
		try {
			InputStream is = new FileInputStream("c:\\tmp\\test-deanonim.txt");
			is.transferTo(out);
			is.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}

}
