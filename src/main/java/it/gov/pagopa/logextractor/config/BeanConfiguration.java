package it.gov.pagopa.logextractor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import it.gov.pagopa.logextractor.exception.RestTemplateExceptionHandler;

@Configuration
public class BeanConfiguration {

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		//restTemplate.setErrorHandler(new RestTemplateExceptionHandler());
		return restTemplate;
	}
}
