package it.gov.pagopa.logextractor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationCsvBean {

	private String IUN;
	private String Data_invio;
	private String Data_generazione_attestazione_opponibile_a_terzi;
	private String Oggetto;
	private String Codici_fiscali;
}
