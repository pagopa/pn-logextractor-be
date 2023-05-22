package it.gov.pagopa.logextractor.rest;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
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
import it.gov.pagopa.logextractor.util.RandomUtils;
import it.gov.pagopa.logextractor.util.external.s3.S3ClientService;

@RestController
@CrossOrigin(allowedHeaders = "password,content-disposition",exposedHeaders = "password,content-disposition")
public class LogController implements LogsApi {


	@Autowired
	LogService logService;
	
	@Autowired
	S3ClientService s3ClientService;
	
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
	public ResponseEntity<BaseResponseDto> currentProcessStatus(String xPagopaPnUid, String xPagopaPnCxType, @RequestParam ("key") String key)
			throws Exception {
		s3ClientService.downloadUrl(key);
		return LogsApi.super.currentProcessStatus(xPagopaPnUid, xPagopaPnCxType, key);
	}

	@Override
	public ResponseEntity<BaseResponseDto> personActivityLogs(String xPagopaPnUid, String xPagopaPnCxType, PersonLogsRequestDto personLogsRequestDto) throws Exception {
		
		String key = personLogsRequestDto.getTicketNumber()+"-"+(new RandomUtils().generateRandomAlphaNumericString())+".zip";
		s3ClientService.signBucket( key );
		BaseResponseDto dto = new BaseResponseDto();
		dto.setMessage(key);
		ResponseEntity<BaseResponseDto> ret = ResponseEntity.status(HttpStatus.OK).body(dto);
		this.threadLocalService.initialize(httpServletResponse, key);
		
		if (Boolean.TRUE.equals(personLogsRequestDto.getDeanonimization())) {
			logService.getDeanonimizedPersonLogs(personLogsRequestDto, xPagopaPnUid, xPagopaPnCxType);
		}else {
			logService.getAnonymizedPersonLogs(personLogsRequestDto, xPagopaPnUid, xPagopaPnCxType); 
		}
//		handleResponse();
		return ret;
	}

	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<BaseResponseDto> handleCustomException(CustomException e){
		this.threadLocalService.remove();
		return ResponseEntity.status(e.getCode()).body(e.getDto());
	}

	@Override
	public ResponseEntity<Resource> notificationInfoLogs(String xPagopaPnUid, String xPagopaPnCxType, NotificationInfoRequestDto notificationInfoRequestDto) throws Exception {
		this.threadLocalService.initialize(httpServletResponse, notificationInfoRequestDto.getTicketNumber());
		logService.getNotificationInfoLogs(notificationInfoRequestDto,xPagopaPnUid, xPagopaPnCxType);
		
		handleResponse();
		return null;
	}

	@Override
	public ResponseEntity<Resource> notificationsInMonth(String xPagopaPnUid, String xPagopaPnCxType, MonthlyNotificationsRequestDto monthlyNotificationsRequestDto) throws Exception {
		this.threadLocalService.initialize(httpServletResponse, monthlyNotificationsRequestDto.getTicketNumber());
		logService.getMonthlyNotifications(monthlyNotificationsRequestDto, xPagopaPnUid, xPagopaPnCxType);
		handleResponse();
		return null;
	}

	@Override
	public ResponseEntity<Resource> processLogs(String xPagopaPnUid, String xPagopaPnCxType, TraceIdLogsRequestDto traceIdLogsRequestDto) throws Exception {
		this.threadLocalService.initialize(httpServletResponse, "processLogs");
		logService.getTraceIdLogs(traceIdLogsRequestDto, xPagopaPnUid, xPagopaPnCxType);
		handleResponse();
		return null;
	}
	
	@Override
	public ResponseEntity<Resource> sessionLogs(String xPagopaPnUid, String xPagopaPnCxType, SessionLogsRequestDto sessionLogsRequestDto) throws Exception {
		this.threadLocalService.initialize(httpServletResponse, sessionLogsRequestDto.getTicketNumber());
		if (Boolean.TRUE.equals(sessionLogsRequestDto.getDeanonimization())) {
			logService.getDeanonimizedSessionLogs(sessionLogsRequestDto, xPagopaPnUid, xPagopaPnCxType);
		}else {
			logService.getAnonymizedSessionLogs(sessionLogsRequestDto, xPagopaPnUid, xPagopaPnCxType);
		}
		handleResponse();
		return null;
	}
}
