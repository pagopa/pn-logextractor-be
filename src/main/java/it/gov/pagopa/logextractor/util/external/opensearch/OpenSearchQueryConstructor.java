package it.gov.pagopa.logextractor.util.external.opensearch;

import it.gov.pagopa.logextractor.util.constant.OpensearchConstants;

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
		if(null != queryData && !queryData.isEmpty()) {
			for(OpenSearchQuerydata currentQueryData : queryData) {
				for (OpenSearchQueryFilter filterTemp : currentQueryData.getMatchFields()) {
					paramsBuilder.append("{\"match\":{"+ "\""+filterTemp.getKey()+"\":"+ "\""
							+filterTemp.getValue()+"\"}},");
			    }
				if(null != currentQueryData.getRangeData()) {
					paramsBuilder.append("{\"range\":{\""+currentQueryData.getRangeData().getRangeField()+"\":{\"gte\":\""
							+currentQueryData.getRangeData().getFrom()+"\",\"lte\":\""
							+currentQueryData.getRangeData().getTo()+"\"}}}");
				}
				else{
					paramsBuilder.deleteCharAt(paramsBuilder.length()-1);
				}
				queryBuilder.append("{\"query\":{\"bool\":{\"filter\":["+paramsBuilder+"]}}");
				if(null != currentQueryData.getSortFilter()) {
					queryBuilder.append(",\"sort\":[{\""+currentQueryData.getSortFilter().getSortField()+"\": "
							+ "{\"order\":\""+currentQueryData.getSortFilter().getSortOrder().toString()+"\"}}]\n");
				}
				queryBuilder.append(",\"size\":" + OpensearchConstants.OS_QUERY_RESULT_PAGE_SIZE + "}\n");
				paramsBuilder.setLength(0);
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
	public OpenSearchQuerydata prepareQueryData(Map<String, Object> searchData, OpenSearchRangeQueryData rangeQueryData, OpenSearchSortFilter sortFilters) {
		ArrayList<OpenSearchQueryFilter> simpleQueryFilters = new ArrayList<>();
		for (Map.Entry<String, Object> entry : searchData.entrySet()) { 
			OpenSearchQueryFilter internalIdFilter = new OpenSearchQueryFilter(entry.getKey(), entry.getValue().toString());
			simpleQueryFilters.add(internalIdFilter);
		}
		return new OpenSearchQuerydata(simpleQueryFilters, rangeQueryData, sortFilters);
	}
}
