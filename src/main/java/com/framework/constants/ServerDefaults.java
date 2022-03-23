package com.framework.constants;

public interface ServerDefaults extends ServerTime{	
	public final int 
	DELETE_IF_INACTIVE_FOR = 1 * year,
	LOGOUT_IF_INACTIVE_FOR = 5 * minute,
	KEEP_REPORTS_FOR = 1 * month,
	ACCOUNT_LOCKDOWN = 3 * minute,
	OTP_EXPIRED_IN = 2 * minute,
	MAX_CONCURRENT_SESSIONS = 4,
	MAX_STORAGE_PER_USER = 2,
	MAX_NUM_DEVICES = 5,
	MAX_CACHE_SIZE = 1000000,
	MAX_REQUEST_PER_MINUTE = 15;
	
	public final boolean 
	FORCE_EMAIL_VERIFICATION = true;
	
	public final String 
	DEFAULT_ENCRYPTION_METHOD = "AES/CBC/PKCS5Padding",
	SERVER_EMAIL = "******@gmail.com",
	SERVER_USER_DATA_PATH = "data/users";
}
