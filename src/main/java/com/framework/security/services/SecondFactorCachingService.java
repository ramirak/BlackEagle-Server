package com.framework.security.services;

import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.framework.constants.ServerDefaults;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

@Service
public class SecondFactorCachingService {
	private Cache<String, String> otpCache;

	public SecondFactorCachingService() {
		this.otpCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS)
				.maximumSize(ServerDefaults.MAX_CACHE_SIZE).build(new CacheLoader<String, String>() {
					@Override
					public String load(String key) throws Exception {
						return generateOTP(key);
					}
				});
	}

	private String generateOTP(String uid) {
		// Generate short random value with 7 digits (only numbers)
		SecureRandom rand = new SecureRandom();
		int otp = 1000000 + rand.nextInt(8999999);
		return String.valueOf(otp);
	}

	public String getOTP(String uid) throws ExecutionException {
		return this.otpCache.get(uid);
	}

	public void removeOTP(String uid) {
		this.otpCache.invalidate(uid);
	}

	public boolean hasKey(String key) {
		if (this.otpCache.asMap().containsKey(key))
			return true;
		return false;
	}

	public void clearCache() {
		otpCache.invalidateAll();
	}
}
