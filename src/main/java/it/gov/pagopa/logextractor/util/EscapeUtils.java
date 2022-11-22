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
     * @return A string containing the csv escaped value
     * */
    public String escapeForCsv(String valueToEscape) {
        if(valueToEscape.startsWith("=")){
            valueToEscape = RegExUtils.replaceFirst(valueToEscape, "=", "'='");
        }
        return StringEscapeUtils.escapeCsv(valueToEscape);
    }
}
