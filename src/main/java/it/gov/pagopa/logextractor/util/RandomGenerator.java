package it.gov.pagopa.logextractor.util;

import java.time.Instant;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomGenerator {

	/**
	 * Generate a random string with format - <local_date_time> - <random_alpha_numeric_string>
	 * @return The randomly generated string
	 * */
	public String generateRandomToken() {
		return Instant.now().toEpochMilli() + "-" + RandomStringUtils.random(10, true, true);
	}
}
