package it.gov.pagopa.logextractor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import it.gov.pagopa.logextractor.util.PasswordFactory;
import it.gov.pagopa.logextractor.util.RandomUtils;
import it.gov.pagopa.logextractor.util.external.s3.S3ClientService;

@RestController
@CrossOrigin(allowedHeaders = "password,content-disposition",exposedHeaders = "password,content-disposition")
public class LogController implements LogsApi {

	@Autowired
	LogService logService;
	
	@Autowired
	S3ClientService s3ClientService;
	
	private  ResponseEntity<BaseResponseDto> prepareResponse(String key, String zipPassword) throws Exception {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Access-Control-Expose-Headers", "password,content-disposition");
		responseHeaders.set("password", zipPassword);
		BaseResponseDto dto = new BaseResponseDto();
		dto.setMessage(key);
		return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).body(dto);
	}

	@Override
	public ResponseEntity<BaseResponseDto> currentProcessStatus(String xPagopaPnUid, String xPagopaPnCxType, @RequestParam ("key") String key)
			throws Exception {
				
				BaseResponseDto dto = new BaseResponseDto();
		dto.message( s3ClientService.downloadUrl(key));

		return ResponseEntity.ok(dto);
	}

	@Override
	public ResponseEntity<BaseResponseDto> personActivityLogs(String xPagopaPnUid, String xPagopaPnCxType, PersonLogsRequestDto personLogsRequestDto) throws Exception {
		String key = generateKey(personLogsRequestDto.getTicketNumber());
		String zipPassword=PasswordFactory.createPassword();
		if (Boolean.TRUE.equals(personLogsRequestDto.getDeanonimization())) {
			logService.getDeanonimizedPersonLogs(key, zipPassword, personLogsRequestDto, xPagopaPnUid, xPagopaPnCxType);
		}else {
			logService.getAnonymizedPersonLogs(key, zipPassword, personLogsRequestDto, xPagopaPnUid, xPagopaPnCxType); 
		}
		return prepareResponse(key, zipPassword);
	}

	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<BaseResponseDto> handleCustomException(CustomException e){
		return ResponseEntity.status(e.getCode()).body(e.getDto());
	}

	@Override
	public ResponseEntity<BaseResponseDto> notificationInfoLogs(String xPagopaPnUid, String xPagopaPnCxType, NotificationInfoRequestDto notificationInfoRequestDto) throws Exception {
		String key = generateKey(notificationInfoRequestDto.getTicketNumber());
		String zipPassword=PasswordFactory.createPassword();
		logService.getNotificationInfoLogs(key, zipPassword,notificationInfoRequestDto,xPagopaPnUid, xPagopaPnCxType);
		return prepareResponse(key, zipPassword);
	}

	@Override
	public ResponseEntity<BaseResponseDto> notificationsInMonth(String xPagopaPnUid, String xPagopaPnCxType, MonthlyNotificationsRequestDto monthlyNotificationsRequestDto) throws Exception {
		String key = generateKey(monthlyNotificationsRequestDto.getTicketNumber());
		String zipPassword=PasswordFactory.createPassword();
		logService.getMonthlyNotifications(key, zipPassword,monthlyNotificationsRequestDto, xPagopaPnUid, xPagopaPnCxType);
		return prepareResponse(key, zipPassword);
	}

	@Override
	public ResponseEntity<BaseResponseDto> processLogs(String xPagopaPnUid, String xPagopaPnCxType, TraceIdLogsRequestDto traceIdLogsRequestDto) throws Exception {
		String key = generateKey(traceIdLogsRequestDto.getTraceId());
		String zipPassword=PasswordFactory.createPassword();
		logService.getTraceIdLogs(key, zipPassword,traceIdLogsRequestDto, xPagopaPnUid, xPagopaPnCxType);
		return prepareResponse(key, zipPassword);
	}
	
	@Override
	public ResponseEntity<BaseResponseDto> sessionLogs(String xPagopaPnUid, String xPagopaPnCxType, SessionLogsRequestDto sessionLogsRequestDto) throws Exception {
		String key = generateKey(sessionLogsRequestDto.getTicketNumber());
		String zipPassword=PasswordFactory.createPassword();
		if (Boolean.TRUE.equals(sessionLogsRequestDto.getDeanonimization())) {
			logService.getDeanonimizedSessionLogs(key, zipPassword,sessionLogsRequestDto, xPagopaPnUid, xPagopaPnCxType);
		}else {
			logService.getAnonymizedSessionLogs(key, zipPassword,sessionLogsRequestDto, xPagopaPnUid, xPagopaPnCxType);
		}
		return prepareResponse(key, zipPassword);
	}
	
	private String generateKey(String base) {
		String ret = base.replaceAll("[+.^:,;?=]","");
		ret +="-"+( RandomUtils.generateRandomAlphaNumericString())+".zip";
		return ret;
	}
}
