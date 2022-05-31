package it.gov.pagopa.logextractor.util.opensearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
		System.out.println("Created query:\n" + queryBuilder.toString());
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
	
	/**
	 * Creates the data structures for a multi-search query from the input map, the map must contain the index name as key and 
	 * its corresponding search filters as values
	 * @param data The input map containing the indexes and their corresponding search filters to be used for a multi-search query
	 * @return A list of OpenSearchQuerydata objects to be used into a multi-search query*/
	public ArrayList<OpenSearchQuerydata> createMultiSearchQueryData(HashMap<String, List<OpenSearchQueryFilter>> data) {
		ArrayList<OpenSearchQuerydata> queryData = new ArrayList<>();
		for (String key : data.keySet()) {
			queryData.add(OpenSearchQuerydata.builder().indexName(key).searchFields(data.get(key)).build());
	    }
		return queryData;
	}
}
