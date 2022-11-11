package it.gov.pagopa.logextractor.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationCsvBean {

	@CsvBindByName(column = "IUN")
	private String iun;
	@CsvBindByName(column = "DATA INVIO")
	private String dataInvio;
	@CsvBindByName(column = "DATA GENERAZIONE ATTESTAZIONE OPPONIBILE A TERZI")
	private String dataGenerazioneAttestazioneOpponibileATerzi;
	@CsvBindByName(column = "OGGETTO")
	private String oggetto;
	@CsvBindByName(column = "CODICI FISCALI")
	private String codiciFiscali;
}
