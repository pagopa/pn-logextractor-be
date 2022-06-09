package it.gov.pagopa.logextractor.util.external.opensearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for constructing OpenSearch queries
 * */
public class OpenSearchQueryConstructor {
	
	/**
	 * Creates a boolean multi-search query string with the input query data
	 * @param queryData The query data
	 * @return A string representing the multi-search boolean query 
	 * */
	public String createBooleanMultiSearchQuery(List<OpenSearchQuerydata> queryData) {
		StringBuilder queryBuilder = new StringBuilder();
		StringBuilder paramsBuilder = new StringBuilder();
		if(null != queryData && queryData.size() > 0) {
			for(OpenSearchQuerydata qTemp : queryData) {
				for (OpenSearchQueryFilter filterTemp : qTemp.getMatchFields()) {
					paramsBuilder.append("{\"match\":{"+ "\""+filterTemp.getKey()+"\":"+ "\""+filterTemp.getValue()+"\"}},");
			    }
				if(null != qTemp.getRangeData()) {
					paramsBuilder.append("{\"range\":{\""+qTemp.getRangeData().getRangeField()+"\":{\"gte\":\""
											+qTemp.getRangeData().getFrom()+"\",\"lte\":\""+qTemp.getRangeData().getTo()+"\"}}}");
				}
				else{
					paramsBuilder.deleteCharAt(paramsBuilder.length()-1);
				}
				queryBuilder.append("{\"index\":\""+qTemp.getIndexName()+"\"}\n"
									+ "{\"query\":{\"bool\":{\"filter\":["+paramsBuilder+"]}}}\n");
				paramsBuilder.setLength(0);
			}
		}
		return queryBuilder.toString();
	}
	
	/**
	 * Creates a simple multi-search query string with the input query data
	 * @param queryData The query data
	 * @return A string representing the multi-search simple query 
	 * */
	public String createSimpleMultiSearchQuery(List<OpenSearchQuerydata> queryData) {
		StringBuilder queryBuilder = new StringBuilder();
		if(null != queryData && queryData.size() > 0) {
			for(OpenSearchQuerydata qTemp : queryData) {
				StringBuilder paramsBuilder = new StringBuilder();
				for (OpenSearchQueryFilter filterTemp : qTemp.getMatchFields()) {
					paramsBuilder.append("{\"match\":{"+ "\""+filterTemp.getKey()+"\":"+ "\""+filterTemp.getValue()+"\"}}");
			    }
				queryBuilder.append("{\"index\":\""+qTemp.getIndexName()+"\"}\n"
									+ "{\"query\":"+paramsBuilder+"}\n");
			}
		}
		return queryBuilder.toString();
	}
	
	/**
	 * Method that prepares query data
	 * @param indexName the name of the index in OpenSearch
	 * @param searchData a map containing the search field and the value of the field
	 * @param rangeQueryData date range, if it is any
	 * @return {@link OpenSearchQuerydata} ready to be passed to the query constructor
	 */
	public OpenSearchQuerydata prepareQueryData(String indexName, Map<String, Object> searchData, OpenSearchRangeQueryData rangeQueryData) {
		ArrayList<OpenSearchQueryFilter> simpleQueryFilters = new ArrayList<>();
		for (Map.Entry<String, Object> entry : searchData.entrySet()) { 
			OpenSearchQueryFilter internalIdFilter = new OpenSearchQueryFilter(entry.getKey(), entry.getValue().toString());
			simpleQueryFilters.add(internalIdFilter);
		}
		return new OpenSearchQuerydata(indexName, simpleQueryFilters, rangeQueryData);
	}
}
