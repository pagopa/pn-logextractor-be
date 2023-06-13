package it.gov.pagopa.logextractor.util;

import java.time.Instant;
import java.util.UUID;

public class RandomUtils {

    /**
     * Generate a random string with <local_date_time> - <random_UUID_string> format
     * @return The randomly generated string
     * */
    public static String generateRandomAlphaNumericString() {
        return Instant.now().toEpochMilli() + "-" + UUID.randomUUID();
    }

    /**
     * Generate a random string with Root=<random_UUID_string> format
     * @return The randomly generated string representing a trace id
     * */
    public static String generateRandomTraceId() {
        return "Root=" + UUID.randomUUID().toString().toLowerCase();
    }
}
