package it.gov.pagopa.logextractor.util.constant;
/**
 * Utility class to list the logging message constants
 */
public class LoggingConstants {
    private LoggingConstants(){}
    public static final String GET_NOTIFICATION_DETAILS = "Getting notification details...";
    public static final String QUERY_EXECUTION_COMPLETED_TIME = "Query execution completed in {} ms, retrieved {} documents, constructing service response...";
    public static final String QEURY_EXECUTION_COMPLETED_TIME_DEANONIMIZE_DOCS = "Query execution completed in {} ms, retrieved {} documents, deanonimizing results...";
    public static final String DEANONIMIZED_RETRIEVE_PROCESS_END = "Deanonimized session logs retrieve process - END in {} ms";
    public  static final String SERVICE_RESPONSE_CONSTRUCTION_TIME = "Service response constructed in {} ms";
    public static final String ANONYMIZED_RETRIEVE_PROCESS_END = "Anonymized logs retrieve process - END in {} ms";
    public static final String QUERY_CONSTRUCTION = "Constructing Opensearch query...";
    public static final String QUERY_EXECUTION = "Executing query:";
    public static final String TRACE_ID_PLACEHOLDER = "trace_id";
}