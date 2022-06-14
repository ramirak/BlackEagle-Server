package com.framework.security.services;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import com.framework.constants.PasswordsDefaults;
import com.framework.constants.ServerDefaults;
import com.framework.exceptions.UnauthorizedRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

@Service
public class BruteForceProtection {
	private Cache<String, Integer> failedLoginAttempts;
	private final int maxAttemptsUID = PasswordsDefaults.MAX_TRIES_UID, maxAttemptsIP = PasswordsDefaults.MAX_TRIES_IP;

	public BruteForceProtection() {
		this.failedLoginAttempts = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
				.maximumSize(ServerDefaults.MAX_CACHE_SIZE * 10).build(new CacheLoader<String, Integer>() {
					@Override
					public Integer load(String key) throws Exception {
						return 0;
					}
				});
	}

	public void bfpCheck(String ipAddress, String uid, boolean success) {
		/**
		 * Block IP after small number of tries [3] to prevent blocking an attacked
		 * account (Account DDOS). If the login attempts exceed a higher number of tries
		 * than max tries per Account [10], the the account should be blocked for the
		 * possibility of systemic attack ..
		 */
		try {
			if (getfailedLoginAttempts(ipAddress) > maxAttemptsIP) {
				getfailedLoginAttempts(uid); // Still need to count UID as a try
				throw new UnauthorizedRequest("IP was blocked due to too many login attempts");
			}
			if (getfailedLoginAttempts(uid) > maxAttemptsUID) {
				throw new UnauthorizedRequest("Account was blocked due to too many login attempts");
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (success) { // Clear login attempts count only if login was success before it was blocked
			removefailedLoginAttempts(uid);
			removefailedLoginAttempts(ipAddress);
		}
	}

	private Integer getfailedLoginAttempts(String uid) throws ExecutionException {
		int count = this.failedLoginAttempts.get(uid);
		this.failedLoginAttempts.asMap().put(uid, count + 1);
		return count + 1;
	}

	private void removefailedLoginAttempts(String uid) {
		this.failedLoginAttempts.invalidate(uid);
	}
}
