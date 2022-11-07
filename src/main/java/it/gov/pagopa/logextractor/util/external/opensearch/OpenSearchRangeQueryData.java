package it.gov.pagopa.logextractor.util.external.opensearch;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OpenSearchRangeQueryData {

	private String rangeField;
	private String from;
	private String to;
}
