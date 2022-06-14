package com.framework.logic.jpa;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.framework.boundaries.UserBoundary;
import com.framework.communication.EmailService;
import com.framework.constants.ServerDefaults;
import com.framework.data.PasswordEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.PasswordDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.AdminService;
import com.framework.logic.converters.UserEntityConverterImplementation;
import com.framework.security.services.PasswordUtils;
import com.framework.utilities.Validations;

@Service
public class AdminServiceJpa implements AdminService {
	private UserDao userDao;
	private UserEntityConverterImplementation ueConverter;
	private Validations utils;
	private EmailService emailService;
	private PasswordUtils passUtils;
	private PasswordEncoder passwordEncoder;

	public AdminServiceJpa() {
	}

	@Autowired
	public void setUtils(Validations utils) {
		this.utils = utils;
	}

	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Autowired
	public void setUeConverter(UserEntityConverterImplementation ueConverter) {
		this.ueConverter = ueConverter;
	}

	@Autowired
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	@Autowired
	public void setPassUtils(PasswordUtils passUtils) {
		this.passUtils = passUtils;
	}

	@Autowired
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserBoundary getSpecificUser(String email) {
		utils.assertAuthorizedOperation("ADMIN");

		Optional<UserEntity> existingUser = userDao.findById(email);
		if (existingUser.isPresent())
			return ueConverter.toBoundary(existingUser.get());
		throw new NotFoundException("User does not exists in the database");
	}

	@Override
	public UserBoundary resetPassword(String email) {
		utils.assertAuthorizedOperation("ADMIN");

		Optional<UserEntity> existingUser = userDao.findById(email);
		if (existingUser.isPresent()) {
			PasswordEntity pe = new PasswordEntity();
			pe.setCreationTime(new Date());
			String newPassword = passUtils.generatePassword();
			String hashedPassword = passwordEncoder.encode(newPassword);
			pe.setPassword(hashedPassword);
			existingUser.get().addPassword(pe);
			emailService.sendEmail(email,
					"Your new generated password is:\n" + newPassword
							+ "\nPlease change it when you log into your account." + "\n\nBlackEagleServices.",
					"BlackEagle password reset");
			userDao.save(existingUser.get());
		}
		throw new NotFoundException("User does not exists in the database");
	}

	@Override
	public UserBoundary deleteAccount(String email) {
		utils.assertAuthorizedOperation("ADMIN");

		Optional<UserEntity> existingUser = userDao.findById(email);
		if (existingUser.isPresent()) {
			for (UserEntity device : existingUser.get().getDevices()) {
				try {
					FileUtils.deleteDirectory(new File(ServerDefaults.SERVER_USER_DATA_PATH + "/" + device.getId()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			userDao.deleteById(email);
			return ueConverter.toBoundary(existingUser.get());
		}
		throw new NotFoundException("User does not exists in the database");
	}
}
