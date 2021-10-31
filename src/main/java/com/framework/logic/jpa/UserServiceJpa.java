package com.framework.logic.jpa;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.framework.boundaries.PasswordBoundary;
import com.framework.boundaries.UserBoundary;
import com.framework.data.PasswordEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.PasswordDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.AlreadyExistingException;
import com.framework.exceptions.ForbiddenException;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.UserService;
import com.framework.logic.converters.PasswordEntityConverterImlementation;
import com.framework.logic.converters.UserEntityConverterImplementation;
import com.framework.security.services.OTPService;
import com.framework.security.services.PasswordUtils;

@Service
public class UserServiceJpa implements UserService {
	private UserDao userDao;
	private PasswordDao passwordDao;
	private UserEntityConverterImplementation ueConverter;
	private PasswordEntityConverterImlementation peConverter;
	private PasswordEncoder passwordEncoder;
	private PasswordUtils passwordUtils;
	private OTPService otpService;

	@Autowired
	public void setOtpService(OTPService otpService) {
		this.otpService = otpService;
	}

	@Autowired
	public void setPasswordUtils(PasswordUtils passwordUtils) {
		this.passwordUtils = passwordUtils;
	}

	@Autowired
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Autowired
	public void setPasswordDao(PasswordDao passwordDao) {
		this.passwordDao = passwordDao;
	}

	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Autowired
	public void setPeConverter(PasswordEntityConverterImlementation peConverter) {
		this.peConverter = peConverter;
	}

	@Autowired
	public void setUeConverter(UserEntityConverterImplementation ueConverter) {
		this.ueConverter = ueConverter;
	}

	@Override
	public UserBoundary register(UserBoundary user) {
		// Hash the password before converting to entity
		String hashedPass = passwordEncoder.encode(user.getUserId().getPasswordBoundary().getPassword());
		user.getUserId().getPasswordBoundary().setPassword(hashedPass);
		// Convert and save to database if the user is not already exits
		UserEntity ue = ueConverter.fromBoundary(user);
		Optional<UserEntity> exitingUser = userDao.findById(user.getUserId().getUID());
		if (exitingUser.isPresent())
			throw new AlreadyExistingException("uid already in the database");
		// Convert and save the password entity
		PasswordEntity pe = peConverter.fromBoundary(user.getUserId().getPasswordBoundary());
		userDao.save(ue);
		passwordDao.save(pe);
		return user;
	}

	@Override
	public UserBoundary login(String userEmail, String password) {
		Optional<UserEntity> exitingUser = userDao.findById(userEmail);
		if (!exitingUser.isPresent())
			throw new NotFoundException("User does not exists in the database");

		UserEntity ue = exitingUser.get();

		return null;
	}

	@Override
	public UserBoundary login2FA(String oneTimeKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserBoundary updateUser(UserBoundary update) {
		// TODO get currently logged-in password details
		String password = " " ;
		
		// Find the corresponding user in the database
		Optional<UserEntity> existingUser = userDao.findById(update.getUserId().getUID());
		if (!existingUser.isPresent())
			throw new NotFoundException("User does not exists in the database");

		UserEntity existingEntity = existingUser.get();
		PasswordBoundary updatedPassBoundary = update.getUserId().getPasswordBoundary();

		// The user requests the deactivation of his account without deleting
		if (update.getActive() != null)
			existingEntity.setActive(update.getActive());
		if (update.getUsername() != null)
			existingEntity.setUsername(update.getUsername());
		
		// The user requests to change his password
		if (updatedPassBoundary != null && updatedPassBoundary.getPassword() != null) {
			// TODO Check authentication details / the user is required to enter is old
			// password ..

			if (!existingEntity.isPasswordInHistory(passwordEncoder.encode(password))) {
				// Check if the new password is a valid password
				if (passwordUtils.checkPassword(password)) {
					// If all the checks are passed, proceed to updating the entity with the new
					// password
					PasswordEntity newPassEntity = peConverter.fromBoundary(updatedPassBoundary);
					existingEntity.addPassword(newPassEntity);
					passwordDao.save(newPassEntity);

					// TODO delete old passwords
				}
			}
		}
		userDao.save(existingEntity);
		return ueConverter.toBoundary(existingEntity);

	}

	@Override
	public UserBoundary resetPassword(String userEmail, String oneTimeKey) {

		return null;
	}

	@Override
	public UserBoundary deleteAccount(String oneTimeKey) {

		String currentlyLoggedInUID = "";
		Optional<UserEntity> existingUser = userDao.findById(currentlyLoggedInUID);
		if (!existingUser.isPresent())
			throw new NotFoundException("User does not exists in the database");

		try {
			// Compare user input to the generated OTP value and delete if equals.
			if (otpService.getOTP(currentlyLoggedInUID) == Integer.parseInt(oneTimeKey)) {
				// TODO delete from dataDao
				// TODO delete owned devices
				passwordDao.deleteById(currentlyLoggedInUID);
				userDao.deleteById(currentlyLoggedInUID);
			} else
				throw new ForbiddenException("One time key does not match user's input");
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return ueConverter.toBoundary(existingUser.get());
	}

}
