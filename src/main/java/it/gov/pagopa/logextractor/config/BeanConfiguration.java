package it.gov.pagopa.logextractor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
		return new RestTemplate(new SimpleClientHttpRequestWithGetBodyFactory());
	}
	
	@Bean
	public RestTemplate simpleRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}
}
