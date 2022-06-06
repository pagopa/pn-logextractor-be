package it.gov.pagopa.logextractor.service;

import java.io.IOException;

import it.gov.pagopa.logextractor.dto.response.DownloadLogResponseDto;

public interface LogService {
	DownloadLogResponseDto getPersonLogs(String dateFrom, String dateTo, String referenceDate, String ticketNumber, Integer uin, String personId, String password) throws IOException;
}
