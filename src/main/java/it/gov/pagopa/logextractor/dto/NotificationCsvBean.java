package it.gov.pagopa.logextractor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationCsvBean {

	private String iun;
	private String data_Invio;
	private String data_Generazione_Attestazione_OpponibileATerzi;
	private String oggetto;
	private String codici_Fiscali;
}
