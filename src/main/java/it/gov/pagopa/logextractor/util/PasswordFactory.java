package it.gov.pagopa.logextractor.util;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

/**
 * Factory class to manage the core operations for passwords
 */
public class PasswordFactory {

	/**
	 * Generates a random password with the given length, number of lower case chars, upper case chars, 
	 * digits and special chars and special char list
	 * @param numberOfLowerChars the minimum number of lower case chars
	 * @param numberOfUpperChars the minimum number of upper case chars
	 * @param numberOfDigits the minimum number of digits
	 * @param specialCharList the list of special chars
	 * @param numberOfSpecialChars the minimum number of special chars
	 * @param length the password length
	 * @return the random generated password with the given specifications
	 * */
	public String createPassword(int numberOfLowerChars, int numberOfUpperChars, int numberOfDigits, 
									String specialCharList, int numberOfSpecialChars, int length) {
		CharacterRule lowerCaseRule = new CharacterRule(EnglishCharacterData.LowerCase);
        lowerCaseRule.setNumberOfCharacters(numberOfLowerChars);
        CharacterRule upperCaseRule = new CharacterRule(EnglishCharacterData.UpperCase);
        upperCaseRule.setNumberOfCharacters(numberOfUpperChars);
        CharacterRule digitRule = new CharacterRule(EnglishCharacterData.Digit);
        digitRule.setNumberOfCharacters(numberOfDigits);
	    CharacterRule splCharRule = new CharacterRule(specialChars(specialCharList));
	    splCharRule.setNumberOfCharacters(numberOfSpecialChars);
	    return new PasswordGenerator().generatePassword(length, splCharRule, lowerCaseRule, upperCaseRule, digitRule);
	}
	
	/**
	 * Generates a special chars CharacterData instance from the input string containing the list of special chars
	 * @param specialCharList the list of special chars
	 * @return the random generated password with the given specifications
	 * */
	private CharacterData specialChars(String specialCharList) {
		return new CharacterData() {
			
			@Override
			public String getErrorCode() {
				return "500";
			}
			
			@Override
			public String getCharacters() {
				return specialCharList;
			}
		};
	}
	
}
