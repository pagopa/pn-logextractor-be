package it.gov.pagopa.logextractor.util;

public class Constants {

	public static final String FISCAL_CODE_PATTERN = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z]{1}[0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z]{1})|([0-9]{11})$";
	public static final String INTERNAL_ID_PATTERN = "[A-Za-z0-9~-_]*";
	public static final String ALPHA_NUMERIC_WITHOUT_SPECIAL_CHAR_PATTERN = "^[a-zA-Z0-9 .-]+$";
	public static final String INPUT_DATE_FORMAT = "/([0-9]{4})-(?:[0-9]{2})-([0-9]{2})/";
	public static final String INPUT_MONTH_FORMAT = "/([0-9]{4})-(?:[0-9]{2})/";
	public static final String PASSWORD_SPECIAL_CHARS = "!@#$%^&()_+";
	public static final String ZIP_ARCHIVE_NAME = "export";
	public static final String FILE_NAME = "dati";
	public static final String TXT_EXTENSION = ".txt";
	public static final String CSV_EXTENSION = ".csv";
	public static final String ZIP_EXTENSION = ".zip";
	public static final String EXPORT_FOLDER = "export/";
	
	//Match all alphanumeric character and predefined wild characters. Password must consists of 16 characters
	public static final String PASSWORD_PATTERN = "^([a-zA-Z0-9!@#$%^&()_+]{16,16})$";
}
