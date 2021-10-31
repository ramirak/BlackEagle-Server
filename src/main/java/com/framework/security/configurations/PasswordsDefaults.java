package com.framework.security.configurations;

import com.framework.datatypes.Times;

public interface PasswordsDefaults extends Times {
	/**Adjust default password configurations
	*/
	public final int 
	PASS_MIN_LENGTH = 10,
	PASS_MAX_LENGTH = 16,
	MAX_TRIES = 3,
	HISTORY = 4,
	PASS_MAX_VALIDITY = 6 * month;
	/**
	Force password rules for characters - 
	*/
	public final boolean
	UPPERCASE = true,
	LOWERCASE = true,
	DIGITS = true,
	SPECIAL = true;

	/** Prevent the use of common words */
	public final boolean 
	PREVENT_DICTIONARY = true;
	
	public final String 
	DICTIONARY_FILE_PATH = " ";
}
