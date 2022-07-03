package it.gov.pagopa.logextractor.dto;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationGeneralData implements Serializable{
	private static final long serialVersionUID = 1L;
	private String iun;
	private String sentAt;
	private String subject;
	private ArrayList<String> recipients;
	
	@Override
	public String toString() {
		return "iun="+this.iun;
	}
}
