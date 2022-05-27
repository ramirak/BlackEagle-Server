package com.framework.constants;

public interface PasswordsDefaults extends ServerTime {
	/**Adjust default password configurations
	*/
	public final int 
	PASS_MIN_LENGTH = 10,
	PASS_MAX_LENGTH = 16,
	MAX_TRIES_IP = 100,
	MAX_TRIES_UID = 100,
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
	PREVENT_DICTIONARY = true,
	FORCE_SECOND_AUTHENTICATION = true;
	
	public final String 
	DICTIONARY_FILE_PATH = "rockyou.dict",
	TEMP_TOKEN = "PRE_AUTH",
	RESET_PASSWORD_TOKEN = "RES_AUTH";
}
