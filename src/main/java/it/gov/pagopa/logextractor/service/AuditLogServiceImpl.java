package it.gov.pagopa.logextractor.service;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    @Override
    public PnAuditLogEvent buildAuditLogEvent(String xPagopaHelpdUid, PnAuditLogEventType pnAuditLogEventType, String message, Object ... arguments) {
        String logMessage = MessageFormatter.arrayFormat(message, arguments).getMessage();
        PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();
        PnAuditLogEvent logEvent;
        logEvent = auditLogBuilder.before(pnAuditLogEventType, "{} - xPagopaHelpdUid={}", logMessage, xPagopaHelpdUid)
                .mdcEntry("xPagopaHelpdUid", xPagopaHelpdUid)
                .build();
        logEvent.log();
        return logEvent;
    }

}
