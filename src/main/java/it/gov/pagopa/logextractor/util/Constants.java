package it.gov.pagopa.logextractor.util;

public class Constants {

	public static final String FISCAL_CODE_PATTERN = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z]{1}[0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z]{1})|([0-9]{11})$";
	public static final String INTERNAL_ID_PATTERN = "[A-Za-z0-9~-_]*";
	public static final String ALPHA_NUMERIC_WITHOUT_SPECIAL_CHAR_PATTERN = "^[\\w(?:à,ò,è,é)]+$";
	public static final String INPUT_DATE_FORMAT = "/([0-9]{4})-(?:[0-9]{2})-([0-9]{2})/";
	public static final String PASSWORD_SPECIAL_CHARS = "!@#$%^&()_+";
}
