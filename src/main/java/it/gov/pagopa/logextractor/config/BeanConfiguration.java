package it.gov.pagopa.logextractor.config;

import javax.net.ssl.HttpsURLConnection;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Configuration class for defining the custom context beans
 */
@Configuration
public class BeanConfiguration {

	@Bean(name = "openSearchRestTemplate")
	@Profile("dev2")
	public RestTemplate openSearchRestTemplateSkipSSLCheck() {
		RestTemplate ret = new RestTemplate(new SimpleClientHttpRequestWithGetBodyFactory());
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
		return ret;
	}

	@Bean
	@Profile("!dev2")
	public RestTemplate openSearchRestTemplate() {
		RestTemplate ret = new RestTemplate(new SimpleClientHttpRequestWithGetBodyFactory());
		return ret;
	}

	@Bean
	@Profile("!dev2")
	public RestTemplate simpleRestTemplate() {
		return new RestTemplate();
	}

	@Bean(name = "simpleRestTemplate")
	@Profile("dev2")
	public RestTemplate simpleRestTemplateSkipChecks() {
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
		return new RestTemplate();
	}

	@Bean
	public RestTemplate downtimeRestTemplate() {
		return new RestTemplateBuilder().errorHandler(new DowntimeRestTemplateErrorHandler()).build();
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
