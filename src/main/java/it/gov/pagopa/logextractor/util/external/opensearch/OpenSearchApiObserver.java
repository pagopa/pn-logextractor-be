package it.gov.pagopa.logextractor.util.external.opensearch;

public interface OpenSearchApiObserver {

	/**
	 * Notify new OpenSearch document
	 * @param document
	 * @param numDoc
	 */
	public void notify(String document, int numDoc);
}
