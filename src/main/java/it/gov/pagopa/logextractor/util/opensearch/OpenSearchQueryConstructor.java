package it.gov.pagopa.logextractor.util.opensearch;

import java.util.ArrayList;


public class OpenSearchQueryConstructor {

	public String createMultiSearchQuery(ArrayList<OpenSearchQuerydata> queryData, boolean isBoolean) {
		return isBoolean ? createBooleanMultiSearchQuery(queryData) : createSimpleMultiSearchQuery(queryData);
	}
	
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
