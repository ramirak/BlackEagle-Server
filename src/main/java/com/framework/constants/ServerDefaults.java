package com.framework.constants;

public interface ServerDefaults extends ServerTime{	
	public final int 
	DELETE_IF_INACTIVE_FOR = 1 * year,
	LOGOUT_IF_INACTIVE_FOR = 5 * minute,
	KEEP_REPORTS_FOR = 1 * month,
	ACCOUNT_LOCKDOWN = 3 * minute,
	OTP_EXPIRED_IN = 2 * minute,
	MAX_CACHE_SIZE = 1000000,
	MAX_REQUEST_PER_MINUTE = 150,
	
	// Quota management for future development
	MAX_STORAGE_FREE_ACCOUNT = 500,
	MAX_STORAGE_PREMUIM_ACCOUNT = 1000,
	MAX_STORAGE_VIP_ACCOUNT = 2000,
	
	MAX_NUM_DEVICES_FREE_ACCOUNT = 5,
	MAX_NUM_DEVICES_PREMIUM_ACCOUNT = 7,
	MAX_NUM_DEVICES_VIP_ACCOUNT = 10;
			
	public final boolean 
	FORCE_EMAIL_VERIFICATION = true,
	SEND_VIA_EMAIL = true;
	
	public final String 
	DEFAULT_ENCRYPTION_METHOD = "AES/CBC/PKCS5Padding",
	SERVER_EMAIL = "blackeagle-services@outlook.com",
	SERVER_USER_DATA_PATH = "data/users",
	FILTER_REDIRECTION = "0.0.0.0";
	
}
