package it.gov.pagopa.logextractor.util;

import java.util.ArrayList;

public enum RecipientTypes {
	PF, PG;
	
	public static ArrayList<String> getValues(){
		ArrayList<String> valuesAsString = new ArrayList<>();
		for(RecipientTypes tempRep : RecipientTypes.values()) { 
	         valuesAsString.add(tempRep.toString());
	    }
		return valuesAsString;
	}
	
	public static boolean isValid(String value) {
		return getValues().contains(value);
	}
}