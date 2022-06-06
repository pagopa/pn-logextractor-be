package it.gov.pagopa.logextractor.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import it.gov.pagopa.logextractor.dto.request.MonthlyNotificationsRequestDto;
import it.gov.pagopa.logextractor.dto.request.NotificationInfoRequestDto;
import it.gov.pagopa.logextractor.dto.request.OpertatorsInfoRequestDto;
import it.gov.pagopa.logextractor.dto.request.PersonLogsRequestDto;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.logextractor.dto.response.PasswordResponseDto;
import it.gov.pagopa.logextractor.service.LogService;
import net.lingala.zip4j.ZipFile;

@RestController
@RequestMapping("/logextractor/v1/logs")
public class LogController {

	@Autowired
	LogService logService;
	
	@Value("${export.zip.archive.txt.file.name}")
	String txtFileName;
	
	@Value("${export.zip.archive.csv.file.name}")
	String csvFileName;

	@PostMapping(value = "/persons", produces="application/zip")
	public void getPersonActivityLogs(@RequestBody PersonLogsRequestDto personLogsDetails, HttpServletResponse response) throws IOException {
		if (personLogsDetails.isDeanonimization()) {
			
		}
		// use case 7 & 8
		ZipFile zipArchive = logService.getPersonLogs(personLogsDetails.getDateFrom(), personLogsDetails.getDateTo(), 
				personLogsDetails.getTicketNumber(), personLogsDetails.getIun(), personLogsDetails.getPersonId(), personLogsDetails.getPassword());
		ServletOutputStream os = response.getOutputStream(); 
		FileInputStream fis = new FileInputStream(zipArchive.getFile());
	    IOUtils.copyLarge(fis, os);
	    fis.close();
	    os.close();
	    zipArchive.removeFile(txtFileName);
	}
	
	
	@PostMapping(value = "/operators", produces="application/zip")
	public void getOperatorsActivityLogs(@RequestBody OpertatorsInfoRequestDto operatorsInfo, HttpServletResponse response) {
	}
	
	@PostMapping(value = "/notifications/info", produces="application/zip")
	public void getNotificationInfoLogs(@RequestBody NotificationInfoRequestDto notificationInfo, HttpServletResponse response){
		
	}
	
	@PostMapping(value = "/notifications/monthly", produces="application/zip")
	public void getNotificationMonthlyLogs(@RequestBody MonthlyNotificationsRequestDto monthlyNotificationsData,
				HttpServletResponse response) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, ParseException{
	}
	
	@GetMapping(value = "/passwords", produces = "application/json")
	public ResponseEntity<PasswordResponseDto> getPassword(){
		return ResponseEntity.ok(logService.createPassword());
	}
}