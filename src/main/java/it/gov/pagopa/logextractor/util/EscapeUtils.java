package it.gov.pagopa.logextractor.util;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.text.StringEscapeUtils;
/**
 * Utility class for escape operations
 * */
public class EscapeUtils {

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
