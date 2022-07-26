package it.gov.pagopa.logextractor.util;

import java.util.ArrayList;

/**
 * Utility enum to list the person types
 */
public enum RecipientTypes {
	PF, PG;
	
	/**
	 * Gets the enum values
	 * @return The enum values as a list of strings
	 * */
	public static ArrayList<String> getValues(){
		ArrayList<String> valuesAsString = new ArrayList<>();
		for(RecipientTypes tempRep : RecipientTypes.values()) { 
	         valuesAsString.add(tempRep.toString());
	    }
		return valuesAsString;
	}
	
	/**
	 * Checks if the input values is present in the enum value list
	 * @param value The input value to check
	 * @return True if the value is present in the enum, false otherwise
	 * */
	public static boolean isValid(String value) {
		return getValues().contains(value);
	}
}