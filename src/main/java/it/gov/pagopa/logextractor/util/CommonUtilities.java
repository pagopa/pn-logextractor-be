package it.gov.pagopa.logextractor.util;

import java.time.Instant;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.text.StringEscapeUtils;


/**
 * Utility class containing common utility methods
 */
public class CommonUtilities {

	/**
	 * Generate a random string with format - <local_date_time> - <random_alpha_numeric_string>
	 * @return The randomly generated string
	 * */
	public String generateRandomToken() {
		return Instant.now().toEpochMilli() + "-" + RandomStringUtils.random(10, true, true);
	}
	
	/**
	 * Generate a random string with format - Root=<random_alpha_numeric_string>
	 * @return The randomly generated string
	 * */
	public String generateRandomTraceId() {
		return "Root=" + RandomStringUtils.random(16, true, true).toLowerCase();
	}
	
	/**
	 * Escape csv and excel problematic characters in the input string
	 * @param valueToEscape The input string to escape
	 * @return A string containing the csv and excel problematic characters escaped
	 * */
	public String escapeForCsv(String valueToEscape) {
		valueToEscape = RegExUtils.replaceAll(valueToEscape, "=", "'='");
		valueToEscape = RegExUtils.replaceAll(valueToEscape, "-", "'-'");
		return StringEscapeUtils.escapeCsv(valueToEscape);
	}
}
