package it.gov.pagopa.logextractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableWebMvc
public class PnLogextractorBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PnLogextractorBeApplication.class, args);
	}
}
