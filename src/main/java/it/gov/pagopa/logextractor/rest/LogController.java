package it.gov.pagopa.logextractor.rest;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.logextractor.exception.CustomException;
import it.gov.pagopa.logextractor.pn_logextractor_be.api.LogsApi;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.BaseResponseDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.PersonLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.SessionLogsRequestDto;
import it.gov.pagopa.logextractor.pn_logextractor_be.model.TraceIdLogsRequestDto;
import it.gov.pagopa.logextractor.service.LogService;
import it.gov.pagopa.logextractor.service.ThreadLocalOutputStreamService;

@RestController
@CrossOrigin(allowedHeaders = "password,content-disposition",exposedHeaders = "password,content-disposition")
public class LogController implements LogsApi {


	@Autowired
	LogService logService;
	
	@Autowired
	private HttpServletResponse httpServletResponse;
	
	@Autowired
	ThreadLocalOutputStreamService threadLocalService;

	private  void handleResponse() throws Exception {
		try {
			threadLocalService.get().flush();
			threadLocalService.get().close();
		} finally {
			threadLocalService.remove();
		}
	}

	@Override
	public ResponseEntity<Resource> personActivityLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, PersonLogsRequestDto personLogsRequestDto
   		 ) throws Exception {
		
		this.threadLocalService.initialize(httpServletResponse, "personActivityLogs");
		
		if (Boolean.TRUE.equals(personLogsRequestDto.getDeanonimization())) {
			logService.getDeanonimizedPersonLogs(personLogsRequestDto, xPagopaUid, xPagopaCxType);
		}else {
			logService.getAnonymizedPersonLogs(personLogsRequestDto, xPagopaUid, xPagopaCxType); 
		}
		handleResponse();
		return null;
	}

	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<BaseResponseDto> handleCustomException(CustomException e){
		return ResponseEntity.status(e.getCode()).body(e.getDto());
	}
	
	@Override
	public ResponseEntity<Resource> notificationInfoLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, NotificationInfoRequestDto notificationInfoRequestDto) throws Exception {
		this.threadLocalService.initialize(httpServletResponse, "notificationInfoLogs");
		logService.getNotificationInfoLogs(notificationInfoRequestDto,xPagopaUid, xPagopaCxType);
		handleResponse();
		return null;
	}

	@Override
	public ResponseEntity<Resource> notificationsInMonth(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, MonthlyNotificationsRequestDto monthlyNotificationsRequestDto) throws Exception {
		this.threadLocalService.initialize(httpServletResponse, "notificationsInMonth");
		logService.getMonthlyNotifications(monthlyNotificationsRequestDto, xPagopaUid, xPagopaCxType);
		handleResponse();
		return null;
	}

	@Override
	public ResponseEntity<Resource> processLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, TraceIdLogsRequestDto traceIdLogsRequestDto) throws Exception {
		this.threadLocalService.initialize(httpServletResponse, "processLogs");
		logService.getTraceIdLogs(traceIdLogsRequestDto, xPagopaUid, xPagopaCxType);
		handleResponse();
		return null;
	}
	
	@Override
	public ResponseEntity<Resource> sessionLogs(@RequestHeader(value="x-pagopa-uid", required=true) String xPagopaUid,
	   		 @RequestHeader(value="x-pagopa-cx-type", required=true) String xPagopaCxType, SessionLogsRequestDto sessionLogsRequestDto) throws Exception {
		this.threadLocalService.initialize(httpServletResponse, "sessionLogs");
		if (Boolean.TRUE.equals(sessionLogsRequestDto.getDeanonimization())) {
			logService.getDeanonimizedSessionLogs(sessionLogsRequestDto, xPagopaUid, xPagopaCxType);
		}else {
			logService.getAnonymizedSessionLogs(sessionLogsRequestDto, xPagopaUid, xPagopaCxType);
		}
		handleResponse();
		return null;
	}
}
