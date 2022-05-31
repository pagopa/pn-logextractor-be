package it.gov.pagopa.logextractor.config;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

public class SimpleClientHttpRequestWithGetBodyFactory extends SimpleClientHttpRequestFactory {

	@Override
	protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
	    super.prepareConnection(connection, httpMethod);
	    if ("GET".equals(httpMethod)) {
	        connection.setDoOutput(true);
	    }
	}
}
