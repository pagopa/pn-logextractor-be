package it.gov.pagopa.logextractor.dto.response.opensearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OpenSearchResponseBaseDto {

	private String timestamp;
	private String version;
	private String message;
	private String logger_name;
	private String thread_name;
	private String level;
	private Integer level_value;
	private String trace_id;
}
