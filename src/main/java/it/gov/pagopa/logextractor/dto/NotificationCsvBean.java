package it.gov.pagopa.logextractor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationCsvBean {

	private String iun;
	private String data_invio;
	private String data_generazione_attestazione_opponibile_a_terzi;
	private String oggetto;
	private String codici_fiscali;
}
