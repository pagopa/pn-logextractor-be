package it.gov.pagopa.logextractor.util.opensearch;

import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OpenSearchQuerydata {

	private String indexName;
	private ArrayList<OpenSearchQueryFilter> searchFields;
}
