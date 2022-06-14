package com.framework.security.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.framework.constants.PasswordsDefaults;

@Service
public class DictionaryAttackPrevention {
	private Set<String> weakPasswords;

	public DictionaryAttackPrevention() {
		weakPasswords = new HashSet<String>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(PasswordsDefaults.DICTIONARY_FILE_PATH)));
			String password;
			while ((password = reader.readLine()) != null) {
				weakPasswords.add(password.toLowerCase());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Boolean isPassInDictionary(String password) {
		if (!PasswordsDefaults.PREVENT_DICTIONARY)
			return false;
		if (weakPasswords.contains(password.toLowerCase())) {
			System.out.println("found");
			return true;
		}
		System.out.println("not found");
		return false;
	}
}
