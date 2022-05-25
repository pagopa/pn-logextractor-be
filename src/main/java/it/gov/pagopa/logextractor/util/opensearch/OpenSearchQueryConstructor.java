package it.gov.pagopa.logextractor.util.opensearch;

import java.util.ArrayList;

/**
 * Utility class for constructing OpenSearch queries
 * */
public class OpenSearchQueryConstructor {

	/**
	 * Creates a simple or boolean multi search query string with the input query data
	 * @param queryData the query data
	 * @param isBoolean if true the method constructs a boolean multi search query otherwise a simple multi search query
	 * @return a string representing the simple or boolean multi search query 
	 * */
	public String createMultiSearchQuery(ArrayList<OpenSearchQuerydata> queryData, boolean isBoolean) {
		return isBoolean ? createBooleanMultiSearchQuery(queryData) : createSimpleMultiSearchQuery(queryData);
	}
	
	/**
	 * Creates a boolean multi search query string with the input query data
	 * @param queryData the query data
	 * @return a string representing the boolean multi search query 
	 * */
	private String createBooleanMultiSearchQuery(ArrayList<OpenSearchQuerydata> queryData) {
		StringBuilder queryBuilder = new StringBuilder();
		if(null != queryData && queryData.size() > 0) {
			for(OpenSearchQuerydata qTemp : queryData) {
				StringBuilder paramsBuilder = new StringBuilder();
				for (OpenSearchQueryFilter filterTemp : qTemp.getSearchFields()) {
					paramsBuilder.append("{\"match\":{"+ "\""+filterTemp.getKey()+"\":"+ "\""+filterTemp.getValue()+"\"}},");
			    }
				paramsBuilder.deleteCharAt(paramsBuilder.length()-1);
				queryBuilder.append("{\"index\":\""+qTemp.getIndexName()+"\"}\n"
									+ "{\"query\":{\"bool\":{\"must\":["+paramsBuilder+"]}}}\n");
			}
		}
		return queryBuilder.toString();
	}
	
	/**
	 * Creates a simple multi search query string with the input query data
	 * @param queryData the query data
	 * @return a string representing the simple multi search query 
	 * */
	private String createSimpleMultiSearchQuery(ArrayList<OpenSearchQuerydata> queryData) {
		StringBuilder queryBuilder = new StringBuilder();
		if(null != queryData && queryData.size() > 0) {
			for(OpenSearchQuerydata qTemp : queryData) {
				StringBuilder paramsBuilder = new StringBuilder();
				for (OpenSearchQueryFilter filterTemp : qTemp.getSearchFields()) {
					paramsBuilder.append("{\"match\":{"+ "\""+filterTemp.getKey()+"\":"+ "\""+filterTemp.getValue()+"\"}}");
			    }
				queryBuilder.append("{\"index\":\""+qTemp.getIndexName()+"\"}\n"
									+ "{\"query\":"+paramsBuilder+"}\n");
			}
		}
		return queryBuilder.toString();
	}
}
