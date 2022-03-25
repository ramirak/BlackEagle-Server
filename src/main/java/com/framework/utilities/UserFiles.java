package com.framework.utilities;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.framework.security.services.DataEncryptionService;

@Component
public class UserFiles {
	private DataEncryptionService des;

	@Autowired
	public void setDes(DataEncryptionService des) {
		this.des = des;
	}

	public void saveUploadedFile(MultipartFile file, String uploadFolder, String filename, boolean encrypt)
			throws IOException {
		if (!file.isEmpty()) {
			byte[] bytes = file.getBytes();
			Files.createDirectories(Paths.get(uploadFolder));
			Path path = Paths.get(uploadFolder + filename);
			if (encrypt)
				try {
					Files.write(path, des.encrypt(bytes));
				} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException
						| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
						| IOException e) {
					e.printStackTrace();
				}
			else
				Files.write(path, bytes);
		}
	}

	public byte[] getUploadedFile(String uploadFolder, String filename, boolean decrypt, boolean encode) {
		Path p = FileSystems.getDefault().getPath(uploadFolder, filename);
		byte[] fileData = null;
		try {
			if (decrypt)
				fileData = des.decrypt(Files.readAllBytes(p), encode); // Decrypt and encode with base64
			else
				fileData = Files.readAllBytes(p);
		} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException
				| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
				| IOException e) {
			e.printStackTrace();
		}
		return fileData;
	}
}
