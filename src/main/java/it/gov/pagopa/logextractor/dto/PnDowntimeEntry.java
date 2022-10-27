package it.gov.pagopa.logextractor.dto;

import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnFunctionality;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnFunctionalityStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
public class PnDowntimeEntry {

    private PnFunctionality functionality;
    private PnFunctionalityStatus status;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
