package it.gov.pagopa.logextractor.util.opensearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Class which represents a search filter in an OpenSearch multi search query
 * */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class OpenSearchQueryFilter {

	private String key;
	private String value;
}
