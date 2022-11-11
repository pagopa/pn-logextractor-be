package it.gov.pagopa.logextractor.util.external.opensearch;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class which represents a search filter in an OpenSearch multi search query
 * */
@Getter
@AllArgsConstructor
public class OpenSearchQueryFilter {

	private String key;
	private String value;
}
