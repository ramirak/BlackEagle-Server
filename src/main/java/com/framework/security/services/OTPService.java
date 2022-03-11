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
public class OTPService {
	private Cache<String, Integer> otpCache;

	public OTPService() {
		this.otpCache = CacheBuilder.newBuilder()
				.expireAfterWrite(30, TimeUnit.SECONDS).maximumSize(ServerDefaults.MAX_CACHE_SIZE)
				.build(new CacheLoader<String, Integer>() {
					@Override
					public Integer load(String key) throws Exception {
						return generateOTP(key);
					}
				});
	}

	public int generateOTP(String uid) {
		SecureRandom rand = new SecureRandom();
		int otp = 1000000 + rand.nextInt(8999999);
		return otp;
	}

	public int getOTP(String uid) throws ExecutionException {
		return this.otpCache.get(uid);
	}

	public void removeOTP(String uid) {
		this.otpCache.invalidate(uid);
	}

	public void clearCache() {
		otpCache.invalidateAll();
	}

}
