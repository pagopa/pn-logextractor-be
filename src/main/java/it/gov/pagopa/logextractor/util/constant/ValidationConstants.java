package it.gov.pagopa.logextractor.util.constant;
/**
 * Utility class to list the backend input validation constants
 */
public class ValidationConstants {
	private ValidationConstants(){}
	public static final String FISCAL_CODE_PATTERN = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z]{1}[0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z]{1})|([0-9]{11})$";
	public static final String INTERNAL_ID_PATTERN = "[A-Za-z0-9~_-]*";
	public static final String ALPHA_NUMERIC_WITHOUT_SPECIAL_CHAR_PATTERN = "^[a-zA-Z0-9-]+$";
	public static final String DOWNTIME_RESPONSE_DATE_PATTERN = "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)";

}
