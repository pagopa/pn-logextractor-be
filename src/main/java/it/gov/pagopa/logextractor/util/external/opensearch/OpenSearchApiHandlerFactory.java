package it.gov.pagopa.logextractor.util.external.opensearch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenSearchApiHandlerFactory {
	@Autowired
	@Qualifier("openSearchRestTemplate")
	RestTemplate client;
	@Value("${external.opensearch.search.url}")
	String opensearchSearchUrl;
	@Value("${external.opensearch.search.followup.url}")
	String opensearchSearchFollowupUrl;
	@Value("${external.opensearch.basicauth.username}")
	String opensearchUsername;
	@Value("${external.opensearch.basicauth.password}")
	String opensearchPassword;

	public OpenSearchApiHandler getOpenSearchApiHanlder() {
		return getOpenSearchApiHanlder((List<OpenSearchApiObserver>)null) ;
	}
	public OpenSearchApiHandler getOpenSearchApiHanlder(OpenSearchApiObserver observer) {
		List<OpenSearchApiObserver> observers = new ArrayList<>();
		observers.add(observer);
		return getOpenSearchApiHanlder(observers) ;
	}
	public OpenSearchApiHandler getOpenSearchApiHanlder(List<OpenSearchApiObserver> observers) {
		return new OpenSearchApiHandler(client, opensearchSearchUrl, opensearchSearchFollowupUrl, opensearchUsername,
				opensearchPassword, observers, 0);
	}
}
