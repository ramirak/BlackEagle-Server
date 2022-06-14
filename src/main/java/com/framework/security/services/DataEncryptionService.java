package com.framework.security.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.framework.constants.ServerDefaults;

@Service
public class DataEncryptionService {
	private final int ivLen = 16;
	private final String algorithm;
	private SecretKey key;

	public DataEncryptionService() {
		this.algorithm = ServerDefaults.DEFAULT_ENCRYPTION_METHOD;
		try {
			byte[] keyBytes = Files.readAllBytes(Paths.get("sec/blackeagle-aes.bin"));
			this.key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] encrypt(byte[] input) throws NoSuchPaddingException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance(algorithm);

		// Create a new random IV
		byte[] iv = new byte[ivLen];
		new SecureRandom().nextBytes(iv);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		// Encrypt the input
		cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
		byte[] cipherText = cipher.doFinal(input);

		// Add the new IV to the beginning of the cipherText
		byte[] cipherWithIv = new byte[iv.length + cipherText.length];
		System.arraycopy(iv, 0, cipherWithIv, 0, iv.length);
		System.arraycopy(cipherText, 0, cipherWithIv, iv.length, cipherText.length);

		// Encode with base64
		return Base64.getEncoder().encode(cipherWithIv);
	}

	public byte[] decrypt(byte[] cipherText, boolean encode) throws NoSuchPaddingException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

		// Base64 decode to get the CipherText with its IV
		byte[] decodedCipher = Base64.getDecoder().decode(cipherText);

		// Byte arrays for splitting the CipherText
		byte[] iv = new byte[ivLen];
		byte[] originalCipherText = new byte[decodedCipher.length - ivLen];

		// Split the iv and the original CipherText
		System.arraycopy(decodedCipher, 0, iv, 0, ivLen);
		System.arraycopy(decodedCipher, ivLen, originalCipherText, 0, originalCipherText.length);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		// Decrypt the CipherText
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
		byte[] plainText = cipher.doFinal(originalCipherText);
		if (encode)
			return Base64.getEncoder().encode(plainText);
		return plainText;
	}
}
