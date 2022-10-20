package it.gov.pagopa.logextractor.util.constant;

/**
 * Utility class to list the Opensearch service constants
 */
public class OpensearchConstants {
    private OpensearchConstants(){}
    public static final String OS_TIMESTAMP_FIELD = "@timestamp";
    public static final String OS_IUN_FIELD = "iun";
    public static final String OS_UID_FIELD = "uid";
    public static final String OS_CX_ID_FIELD = "cx_id";
    public static final String OS_TRACE_ID_FIELD = "root_trace_id";
    public static final String OS_SEARCH_QUERY_SUFFIX = "/pn-logs/_search";
    public static final String OS_SCROLL_ID_SUFFIX = "_scroll_id";
    public static final int OS_QUERY_RESULT_PAGE_SIZE = 10000;
    public static final String OS_SCROLL_SUFFIX = "/_search/scroll";
    public static final String OS_SCROLL_PARAMETER ="scroll";
    public static final String OS_SCROLL_ID_VALIDITY_DURATION ="10m";
    public static final String UID_PF_PREFIX = "PF-";
    public static final String UID_PG_PREFIX = "PG-";
}
