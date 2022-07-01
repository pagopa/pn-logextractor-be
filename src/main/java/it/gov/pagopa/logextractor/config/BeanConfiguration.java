package it.gov.pagopa.logextractor.config;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for defining the custom context beans
 * */
@Configuration
public class BeanConfiguration {

	@Bean
	public RestTemplate openSearchRestTemplate() {
		//TODO: START -  to delete when deploying in dev environment, this is just for local test purposes
		/*HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			
			@Override
		    public boolean verify(String hostname, SSLSession session) {
		        return true;
		    }
		});*/
		//END
		return new RestTemplate(new SimpleClientHttpRequestWithGetBodyFactory());
	}
	
	@Bean
	public RestTemplate simpleRestTemplate() {
		//TODO: START -  to delete when deploying in dev environment, this is just for local test purposes
		/*HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			
			@Override
		    public boolean verify(String hostname, SSLSession session) {
		        return true;
		    }
		});*/
		//END
		return new RestTemplate();
	}
}
