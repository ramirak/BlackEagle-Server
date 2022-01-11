package com.framework.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UserFiles {
	
	public void saveUploadedFile(MultipartFile file, String uploadFolder, String filename) throws IOException {
	    if (!file.isEmpty()) {
	        byte[] bytes = file.getBytes();
	        Files.createDirectories(Paths.get(uploadFolder));
	        Path path = Paths.get(uploadFolder + filename);
	        Files.write(path, bytes);
	    }
	}
	
}
