package it.gov.pagopa.logextractor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OpensearchScrollQueryDto {
    private String scroll;
    @JsonProperty("scroll_id")
    private String scrollId;
}
