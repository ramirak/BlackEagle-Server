package com.framework.security.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.framework.constants.PasswordsDefaults;
import com.framework.data.PasswordEntity;

@Component
public class PasswordValidations {

	private PasswordEncoder passwordEncoder;
	
	@Autowired
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
	
	public boolean isPasswordInHistory(String rawPassword, Set<PasswordEntity> passwords) {
		for (PasswordEntity pe : passwords) {
			if (passwordEncoder.matches(rawPassword, pe.getPassword()))
				return true;
		}
		return false;
	}

	
	public Boolean isPassInDictionary(String p) {
		if (!PasswordsDefaults.PREVENT_DICTIONARY)
			return false;
		try {
			File f = new File(PasswordsDefaults.DICTIONARY_FILE_PATH);
			Scanner reader = new Scanner(f);
			while (reader.hasNextLine()) {
				String data = reader.nextLine();
				if (p.toLowerCase().contentEquals(data.toLowerCase())) {
					reader.close();
					return true;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return false;
	}

	public int passMinLength = PasswordsDefaults.PASS_MIN_LENGTH;
	public int passMaxLength = PasswordsDefaults.PASS_MAX_LENGTH;
	public int maxTries = PasswordsDefaults.MAX_TRIES;

	public boolean checkMail(String email) {
		boolean isValid = false;
		try {
			InternetAddress internetAddress = new InternetAddress(email);
			internetAddress.validate();
			isValid = true;
		} catch (AddressException e) {
			System.out.println("illegal Mail: " + email);
		}
		return isValid;
	}

	public boolean checkPassword(String pass) {
		if (pass.length() < passMinLength)
			return false;
		if (PasswordsDefaults.UPPERCASE && !stringHasCharInRange(pass, 'A', 'Z'))
			return false;
		if (PasswordsDefaults.LOWERCASE && !stringHasCharInRange(pass, 'a', 'z'))
			return false;
		if (PasswordsDefaults.DIGITS && !stringHasCharInRange(pass, '0', '9'))
			return false;
		if (PasswordsDefaults.SPECIAL && !stringHasCharInRange(pass, "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"))
			return false;
		return true;
	}

	public boolean stringHasCharInRange(String str, char startAscii, char endAscii) {
		for (char i = startAscii; i <= endAscii; i++) {
			if (str.indexOf(i) != -1)
				return true;
		}
		return false;
	}

	public boolean stringHasCharInRange(String str, CharSequence seq) {
		for (int i = 0; i < seq.length(); i++) {
			if (str.indexOf(seq.charAt(i)) != -1)
				return true;
		}
		return false;
	}

}
