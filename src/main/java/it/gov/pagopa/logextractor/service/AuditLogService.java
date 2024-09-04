package it.gov.pagopa.logextractor.service;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;

public interface AuditLogService {

    PnAuditLogEvent buildAuditLogEvent(String xPagopaHelpdUid, PnAuditLogEventType pnAuditLogEventType, String message, Object... arguments);
}
