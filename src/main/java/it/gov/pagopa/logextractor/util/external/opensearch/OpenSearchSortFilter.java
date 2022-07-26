package it.gov.pagopa.logextractor.util.external.opensearch;


import it.gov.pagopa.logextractor.util.SortOrders;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OpenSearchSortFilter {

	private String sortField;
	private SortOrders sortOrder;
}
