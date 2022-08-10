package it.gov.pagopa.logextractor.util;

public class ValidationConstants {
	
	public static final String FISCAL_CODE_PATTERN = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z]{1}[0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z]{1})|([0-9]{11})$";
	public static final String INTERNAL_ID_PATTERN = "[A-Za-z0-9~_-]*";
	public static final String ALPHA_NUMERIC_WITHOUT_SPECIAL_CHAR_PATTERN = "^[a-zA-Z0-9-]+$";
	public static final String IUN_PATTERN = "([A-Za-z]{4})-([A-Za-z]{4})-([A-Za-z]{4})-([0-9]{6})-([A-Za-z]{1})-([0-9]{1})";
	public static final String INPUT_DATE_FORMAT = "([0-9]{4})-(?:[0-9]{2})-([0-9]{2})";
	public static final String INPUT_MONTH_FORMAT = "([0-9]{4})-(?:[0-9]{2})-01T00:00:00.000Z";
	public static final String PASSWORD_SPECIAL_CHARS = "!@#$%^&()_+";
	public static final String PROBLEM_ERROR_CODE_PATTERN = "^[0-9]{3}-[0-9]{4}$";
	public static final String PROBLEM_TITLE_PATTERN = "^[ -~]{0,64}$";
	public static final String PROBLEM_DETAIL_PATTERN = "^.{0,1024}$";
}
