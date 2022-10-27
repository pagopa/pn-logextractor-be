package it.gov.pagopa.logextractor.dto.response;

import it.gov.pagopa.logextractor.dto.PnDowntimeEntry;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PnFunctionality;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class DowntimeCurrentStatusResponseDto {

    private List<PnFunctionality> functionalities = new ArrayList<>();
    private List<PnDowntimeEntry> openIncidents = new ArrayList<>();
}
