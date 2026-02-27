package it.gov.pagopa.logextractor.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // sostituisce @EnableGlobalMethodSecurity
public class WebSecurityConfig {

	@Value("${cors.origin.allowed}")
	String allowedOrigin;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable) // sostituisce csrf().ignoringAntMatchers("/**")
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.exceptionHandling(ex -> {}) // opzionale, puoi personalizzare qui
				.headers(headers -> headers
						.contentSecurityPolicy(csp -> csp
								.policyDirectives("default-src 'none'; script-src 'self'; connect-src 'self'; " +
										"img-src 'self'; style-src 'self'; frame-ancestors 'none'; form-action 'self'"))
						.contentTypeOptions(ct -> {})
						.xssProtection(xss -> xss.disable())
						.frameOptions(frame -> frame.deny())
				)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.authorizeHttpRequests(auth -> auth
						.anyRequest().permitAll() // sostituisce antMatchers("/").permitAll()
				);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.addAllowedOrigin(allowedOrigin);
		configuration.setAllowedMethods(Collections.singletonList("POST, PUT, GET, OPTIONS, DELETE"));
		configuration.addAllowedHeader("*");
		configuration.addAllowedMethod("*");
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}