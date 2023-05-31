package it.gov.pagopa.logextractor.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.external.opensearch.OpenSearchApiObserver;

public class FilenameCollector implements OpenSearchApiObserver {

	private Set<String> names = new HashSet<String>();
	
	@Override
	public void notify(String document, int numDoc) {
		JsonUtilities jsonUtils = new JsonUtilities();
		String date = jsonUtils.getValue(document, "@timestamp");
		String jti = jsonUtils.getValue(document, "jti");
		if(StringUtils.isNotBlank(date) && StringUtils.isNotBlank(jti)) {
			String name = String.format("%s-%s.json", jti,
					LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate().toString());
			names.add(name);
		}
	}

	public Set<String> getNames() {
		return names;
	}
}
