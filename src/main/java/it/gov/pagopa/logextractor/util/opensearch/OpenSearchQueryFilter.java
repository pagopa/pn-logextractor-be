package it.gov.pagopa.logextractor.util.opensearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OpenSearchQueryFilter {

	private String key;
	private String value;
}
