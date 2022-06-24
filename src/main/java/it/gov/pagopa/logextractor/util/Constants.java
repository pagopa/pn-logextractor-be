package it.gov.pagopa.logextractor.util;

public class Constants {

	public static final String FISCAL_CODE_PATTERN = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z]{1}[0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z]{1})|([0-9]{11})$";
	public static final String INTERNAL_ID_PATTERN = "[A-Za-z0-9~_-]*";
	public static final String ALPHA_NUMERIC_WITHOUT_SPECIAL_CHAR_PATTERN = "^[a-zA-Z0-9-]+$";
	public static final String IUN_PATTERN = "([A-Za-z]{4})-([A-Za-z]{4})-([A-Za-z]{4})-([0-9]{6})-([A-Za-z]{1})-([0-9]{1})";
	public static final String INPUT_DATE_FORMAT = "([0-9]{4})-(?:[0-9]{2})-([0-9]{2})";
	public static final String INPUT_MONTH_FORMAT = "([0-9]{4})-(?:[0-9]{2})";
	public static final String PASSWORD_SPECIAL_CHARS = "!@#$%^&()_+";
	public static final String ZIP_ARCHIVE_NAME = "export";
	public static final String LEGAL_FACT_FILE_NAME="legalFact";
	public static final String NOTIFICAION_DOCUMENT_FILE_NAME="notificationDoc";
	public static final String LOG_FILE_NAME = "dati";
	public static final String TXT_EXTENSION = ".txt";
	public static final String CSV_EXTENSION = ".csv";
	public static final String ZIP_EXTENSION = ".zip";
	public static final String EXPORT_FOLDER = "export/";
	public static final String PROBLEM_ERROR_CODE_PATTERN = "^[0-9]{3}-[0-9]{4}$";
	public static final String PROBLEM_TITLE_PATTERN = "^[ -~]{0,64}$";
	public static final String PROBLEM_DETAIL_PATTERN = "^.{0,1024}$";
}
