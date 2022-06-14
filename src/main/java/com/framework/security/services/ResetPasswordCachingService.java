package com.framework.security.services;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.framework.constants.ServerDefaults;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

@Service
public class ResetPasswordCachingService {
	private Cache<String, String> otkCache;
	private PasswordUtils passUtils;

	@Autowired
	public void setPassUtils(PasswordUtils passUtils) {
		this.passUtils = passUtils;
	}

	public ResetPasswordCachingService() {
		this.otkCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS)
				.maximumSize(ServerDefaults.MAX_CACHE_SIZE).build(new CacheLoader<String, String>() {
					@Override
					public String load(String key) throws Exception {
						return generateOTK(key);
					}
				});
	}

	private String generateOTK(String uid) {
		// Generate long random password
		return this.passUtils.generatePassword();
	}

	public String getOTK(String uid) throws ExecutionException {
		return this.otkCache.get(uid);
	}

	public void removeOTK(String uid) {
		this.otkCache.invalidate(uid);
	}

	public boolean hasKey(String key) {
		if (this.otkCache.asMap().containsKey(key))
			return true;
		return false;
	}

	public void clearCache() {
		otkCache.invalidateAll();
	}
}
