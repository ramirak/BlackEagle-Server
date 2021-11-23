package com.framework.security.services;

import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.framework.security.configurations.ServerDefaults;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

@Service
public class OTPService {
	private Cache<String, Integer> otpCache;
	public OTPService() {
		this.otpCache = CacheBuilder.newBuilder()
			     //  .expireAfterWrite(ServerDefaults.OTP_EXPIRED_IN, TimeUnit.SECONDS)
			       .expireAfterAccess(0, TimeUnit.SECONDS)
			       .maximumSize(ServerDefaults.MAX_CACHE_SIZE)
			       .build(
			           new CacheLoader<String, Integer>() {
						@Override
						public Integer load(String key) throws Exception {
							return 0;
						}
			           });
	}
	
	public int generateOTP(String uid) {
		SecureRandom rand = new SecureRandom();
		int otp = 1000000 + rand.nextInt(8999999); 
		this.otpCache.asMap().put(uid, otp);
		return otp;
	}
	
	public int getOTP(String uid) throws ExecutionException {
		return this.otpCache.asMap().get(uid);
	}
	
	public void removeOTP(String uid) {
		this.otpCache.invalidate(uid);
	}
	
	public void clearCache() {
		otpCache.invalidateAll();
	}

}
