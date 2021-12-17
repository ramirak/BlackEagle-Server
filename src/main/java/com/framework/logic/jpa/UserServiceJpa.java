package com.framework.logic.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.framework.boundaries.PasswordBoundary;
import com.framework.boundaries.UserBoundary;
import com.framework.data.EventEntity;
import com.framework.data.PasswordEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.EventDao;
import com.framework.data.dao.PasswordDao;
import com.framework.data.dao.UserDao;
import com.framework.datatypes.EventType;
import com.framework.datatypes.UserRole;
import com.framework.exceptions.AlreadyExistingException;
import com.framework.exceptions.NotFoundException;
import com.framework.exceptions.UnauthorizedRequest;
import com.framework.logic.UserService;
import com.framework.logic.converters.PasswordEntityConverterImlementation;
import com.framework.logic.converters.UserEntityConverterImplementation;
import com.framework.security.services.OTPService;
import com.framework.security.services.PasswordValidations;
import com.framework.utilities.Validations;

@Service
public class UserServiceJpa implements UserService {
	private UserDao userDao;
	private PasswordDao passwordDao;
	private EventServiceJpa eventServiceJpa;
	private UserEntityConverterImplementation ueConverter;
	private PasswordEntityConverterImlementation peConverter;
	private PasswordEncoder passwordEncoder;
	private PasswordValidations passwordRules;
	private OTPService otpService;
	private Validations utils;

	public UserServiceJpa() {
	}

	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Autowired
	public void setEventServiceJpa(EventServiceJpa eventServiceJpa) {
		this.eventServiceJpa = eventServiceJpa;
	}

	@Autowired
	public void setOtpService(OTPService otpService) {
		this.otpService = otpService;
	}

	@Autowired
	public void setPasswordUtils(PasswordValidations passwordUtils) {
		this.passwordRules = passwordUtils;
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
	public void setPeConverter(PasswordEntityConverterImlementation peConverter) {
		this.peConverter = peConverter;
	}

	@Autowired
	public void setUeConverter(UserEntityConverterImplementation ueConverter) {
		this.ueConverter = ueConverter;
	}

	@Autowired
	public void setUtils(Validations utils) {
		this.utils = utils;
	}

	@Override
	@Transactional
	public UserBoundary register(UserBoundary user) {
		utils.assertNull(user);
		utils.assertNull(user.getUserId());
		utils.assertEmptyString(user.getUserId().getUID());
		utils.assertNull(user.getUserId().getPasswordBoundary());
		utils.assertEmptyString(user.getUserId().getPasswordBoundary().getPassword());
		utils.assertEmptyString(user.getUserId().getPasswordBoundary().getHint());

		user.setRole(UserRole.PLAYER);
		user.setActive(true);
		// Hash the password before converting to entity
		String hashedPass = passwordEncoder.encode(user.getUserId().getPasswordBoundary().getPassword());
		user.getUserId().getPasswordBoundary().setPassword(hashedPass);
		// Convert and save to database if the user is not already exits
		UserEntity ue = ueConverter.fromBoundary(user);
		Optional<UserEntity> exitingUser = userDao.findById(user.getUserId().getUID());
		if (exitingUser.isPresent())
			throw new AlreadyExistingException("uid already in the database");
		user.getUserId().getPasswordBoundary().setCreationTime(new Date());
		// Convert and save the password entity
		PasswordEntity pe = peConverter.fromBoundary(user.getUserId().getPasswordBoundary());
		ue.addPassword(pe);
		userDao.save(ue);

		return user;
	}

	@Override
	public UserBoundary login2FA(String oneTimeKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserBoundary updateUser(UserBoundary update) {
		// TODO get currently logged-in password details
		String authenticatedUser = SecurityContextHolder.getContext().getAuthentication().getName();
		boolean dirty = false;
		utils.assertOwnership(authenticatedUser, update.getUserId().getUID());

		// Find the corresponding user in the database
		UserEntity existingEntity = userDao.findById(update.getUserId().getUID())
				.orElseThrow(() -> new NotFoundException("User not found: "));

		PasswordBoundary updatedPassBoundary = update.getUserId().getPasswordBoundary();
		String newPassword = updatedPassBoundary.getPassword();

		// The user requests the deactivation of his account without deleting
		if (update.getActive() != null) {
			existingEntity.setActive(update.getActive());
			dirty = true;
		}
		// The user requests to change his password
		if (updatedPassBoundary != null && newPassword != null && updatedPassBoundary.getHint() != null) {
			utils.assertEmptyString(updatedPassBoundary.getHint());
			// TODO Check authentication details / the user is required to enter his old
			// password in the optional input
			if (!passwordEncoder.matches(update.getUserId().getPasswordBoundary().getOptionalPassword(),
					existingEntity.getActivePasswordEntity().getPassword()))
				throw new UnauthorizedRequest("Failed to verify old password");
			if (!passwordRules.isPasswordInHistory(newPassword, existingEntity.getPasswords())) {
				// Check if the new password is a valid password
				if (passwordRules.checkPassword(newPassword)) {
					// If all the checks are passed, proceed to updating the entity with the new
					// password
					updatedPassBoundary.setPassword(passwordEncoder.encode(newPassword));
					updatedPassBoundary.setCreationTime(new Date());
					PasswordEntity newPassEntity = peConverter.fromBoundary(updatedPassBoundary);
					Optional<PasswordEntity> oldPasswordEntity = existingEntity.addPassword(newPassEntity);
					if (oldPasswordEntity.isPresent())
						passwordDao.delete(oldPasswordEntity.get());
					eventServiceJpa.createEvent(authenticatedUser, EventType.PASSWORD_UPDATE);
					dirty = true;
				}
			}else
				throw new AlreadyExistingException("Password was already used in the past");
		}
		if (dirty)
			userDao.save(existingEntity);
		return ueConverter.toBoundary(existingEntity);
	}

	@Override
	public UserBoundary resetPassword(String userEmail, String oneTimeKey) {

		return null;
	}

	@Override
	public UserBoundary deleteAccount(String oneTimeKey) {
		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.getUsername();
		Optional<UserEntity> existingUser = userDao.findById(authenticatedUser);
		if (!existingUser.isPresent())
			throw new NotFoundException("User does not exists in the database");

		try {
			// Compare user input to the generated OTP value and delete if equals.
			if (otpService.getOTP(authenticatedUser) == Integer.parseInt(oneTimeKey)) {
				// TODO delete from dataDao
				// TODO delete owned devices
				passwordDao.deleteById(authenticatedUser);
				userDao.deleteById(authenticatedUser);
			} else
				throw new UnauthorizedRequest("One time key does not match user's input");
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return ueConverter.toBoundary(existingUser.get());
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserEntity> exitingUser = userDao.findById(username);
		if (!exitingUser.isPresent())
			throw new NotFoundException("User does not exists in the database");
		UserEntity ue = exitingUser.get();

		ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>();

		// Only one role per user, set current saved role
		grantedAuthorities.add(new SimpleGrantedAuthority(ue.getRole()));

		return new org.springframework.security.core.userdetails.User(ue.getUid(),
				ue.getActivePasswordEntity().getPassword(), grantedAuthorities);
	}

}
