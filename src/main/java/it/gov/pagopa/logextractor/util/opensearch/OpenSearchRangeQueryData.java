package it.gov.pagopa.logextractor.util.opensearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenSearchRangeQueryData {

	private String rangeField;
	private String from;
	private String to;
}
