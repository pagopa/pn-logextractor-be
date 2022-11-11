package it.gov.pagopa.logextractor.util.external.opensearch;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class which contains all the query fields required for an OpenSearch multi search query
 * */
@Getter
@AllArgsConstructor
public class OpenSearchQuerydata {
	private List<OpenSearchQueryFilter> matchFields;
	private OpenSearchRangeQueryData rangeData;
	private OpenSearchSortFilter sortFilter;
}
