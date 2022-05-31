package it.gov.pagopa.logextractor.util.opensearch;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Class which contains all the query fields required for an OpenSearch multi search query
 * */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class OpenSearchQuerydata {
	
	private String indexName;
	private List<OpenSearchQueryFilter> matchFields;
	private OpenSearchRangeQueryData rangeData;
}
