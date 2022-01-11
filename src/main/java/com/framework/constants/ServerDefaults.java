package com.framework.constants;

public interface ServerDefaults extends ServerTime{
	public final String ENC_METHODS[] = { 
			"AES/CBC/NoPadding", 
			"AES/CBC/PKCS5Padding", 
			"AES/ECB/NoPadding",
			"AES/ECB/PKCS5Padding", 
			"AES/GCM/NoPadding", 
			"RSA/ECB/PKCS1Padding",
			"RSA/ECB/OAEPWithSHA-1AndMGF1Padding", 
			"RSA/ECB/OAEPWithSHA-256AndMGF1Padding" 
			};
	
	public final int 
	DELETE_IF_INACTIVE_FOR = 1 * year,
	LOGOUT_IF_INACTIVE_FOR = 5 * minute,
	KEEP_REPORTS_FOR = 1 * month,
	ACCOUNT_LOCKDOWN = 3 * minute,
	OTP_EXPIRED_IN = 2 * minute,
	MAX_CONCURRENT_SESSIONS = 4,
	MAX_STORAGE_PER_USER = 2,
	MAX_NUM_DEVICES = 5,
	MAX_CACHE_SIZE = 1000000;
	
	public final boolean 
	FORCE_EMAIL_VERIFICATION = true;
	
	public final String 
	DEFAULT_ENCRYPTION_METHOD = ENC_METHODS[1],
	SERVER_EMAIL = "******@gmail.com",
	SERVER_EMAIL_PASSWORD = "123",
	SERVER_USER_DATA_PATH = "data/users";
}
