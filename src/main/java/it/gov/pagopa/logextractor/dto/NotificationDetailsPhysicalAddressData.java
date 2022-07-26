package it.gov.pagopa.logextractor.dto;

import lombok.Getter;

@Getter
public class NotificationDetailsPhysicalAddressData {

	private String at;
	private String address;
	private String addressDetails;
	private String zip;
	private String municipality;
	private String municipalityDetails;
	private String province;
	private String foreignState;
}
