package com.framework.security.services;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class RandHashingService {
	private String optionalChars;
	private final char start = ' ';
	private final char end = '~';
	private final int randLen = 16;

	public RandHashingService() {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end - start; i++) {
			sb.append(Integer.toString(i).charAt(0));
		}
		this.optionalChars = sb.toString();
	}

	private String generateRandomString() {
		SecureRandom rand = new SecureRandom();
		StringBuilder randomString = new StringBuilder();
		for (int i = 0; i < randLen; i++) {
			int currentIndex = rand.nextInt(optionalChars.length());
			randomString.append(optionalChars.charAt(currentIndex));
		}
		return randomString.toString();
	}

	public String generateSHA256Value() {
		String randomStr = generateRandomString();
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(randomStr.getBytes("UTF-8"));
			return Base64.getEncoder().encodeToString(hash).replace('/', '-');
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
