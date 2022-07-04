package it.gov.pagopa.logextractor.dto;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LegalFactData {

	private String timestamp;
	private ArrayList<LegalFactBasicData> basicData = new ArrayList<>();
}
