package it.gov.pagopa.logextractor.util;

/**
 * Utility class to list the application constants
 */
public class Constants {
	
	//Generic
	public static final String ZIP_ARCHIVE_NAME = "export";
	public static final String LOG_FILE_NAME = "dati";
	public static final String NOTIFICATION_CSV_FILE_NAME = "notifiche";
	public static final String TXT_EXTENSION = ".txt";
	public static final String PDF_EXTENSION = ".pdf";
	public static final String CSV_EXTENSION = ".csv";
	public static final String ZIP_EXTENSION = ".zip";
	public static final String EXPORT_FOLDER = "export/";
	public static final String COGNITO_CUSTOM_ATTRIBUTE_PREFIX = "custom:";
	public static final String MINUTES_LABEL = " minuti";
	public static final String MINUTE_LABEL = " minuto";
	public static final String DOCUMENT_LABEL = "Document";
	public static final String SAFESTORAGE_PREFIX = "safestorage://";
	public static final int PAGE_SIZE = 1000;
	public static final String SPECIAL_CHARS = "!@#$%^&()_+";
	public static final String ERROR_MESSAGE_KEY = "message";
	public static final int CSV_FILE_MAX_ROWS = 200000;
	
	//Opensearch
	public static final String OS_TIMESTAMP_FIELD = "@timestamp";
	public static final String OS_IUN_FIELD = "iun";
	public static final String OS_UID_FIELD = "uid";
	public static final String OS_CX_ID_FIELD = "cx_id";
	public static final String OS_TRACE_ID_FIELD = "root_trace_id";
	public static final String OS_MULTI_SEARCH_SUFFIX = "/_msearch";
	public static final String UID_APIKEY_PREFIX = "APIKEY-";
	public static final String QUERY_INDEX_ALIAS = "pn-logs";
	
	//External PN services
	public static final String EXT_END_DATE_PARAM = "endDate";
	public static final String EXT_START_DATE_PARAM = "startDate";
	public static final String EXT_SENDER_ID_PARAM = "senderId";
	public static final String EXT_SIZE_PARAM = "size";
	public static final String EXT_NEXT_PAGE_KEY_PARAM = "nextPagesKey";
	public static final String EXT_NUM_RECIPIENTS_PARAM = "numberOfRecipients";
	public static final String EXT_CREATED_AT_PARAM = "createdAt";
	public static final String EXT_INTERNAL_ID_PARAM = "internalId";
	public static final String EXT_PA_NAME_PARAM = "paNameFilter";
	
	//Cognito
	public static final String COG_ACTOKEN = "AccessToken";	
}
