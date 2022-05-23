package it.gov.pagopa.logextractor.dto;

import java.util.Date;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NotificationCsvBean {

	private String iun;
	@CsvDate
	private Date sendDate;
	@CsvDate
	private Date attestationGenerationDate;
	private String subject;
	private String taxId;
}
