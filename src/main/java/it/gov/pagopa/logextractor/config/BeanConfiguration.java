package it.gov.pagopa.logextractor.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
/**
 * Configuration class for defining the custom context beans
 * */
@Configuration
public class BeanConfiguration {

	@Bean
	public RestTemplate openSearchRestTemplate() {
		//TODO: START -  to delete when deploying in dev environment, this is just for local test purposes
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
		return new RestTemplate(new SimpleClientHttpRequestWithGetBodyFactory());
	}
	
	@Bean
	public RestTemplate simpleRestTemplate() {
		//TODO: START -  to delete when deploying in dev environment, this is just for local test purposes
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
		return new RestTemplate();
	}

	@Bean
	public ObjectMapper simpleObjectMapper() {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModule(new Jdk8Module());
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}
}
