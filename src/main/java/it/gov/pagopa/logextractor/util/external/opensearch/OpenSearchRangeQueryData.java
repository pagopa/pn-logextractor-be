package it.gov.pagopa.logextractor.util.external.opensearch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenSearchRangeQueryData {

	private String rangeField;
	private String from;
	private String to;
}
