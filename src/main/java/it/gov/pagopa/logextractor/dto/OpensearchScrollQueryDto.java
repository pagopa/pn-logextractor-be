package it.gov.pagopa.logextractor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OpensearchScrollQueryDto {
    private String scroll;
    private String scroll_id;
}
