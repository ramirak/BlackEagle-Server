package com.framework.logic.jpa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.framework.boundaries.DataBoundary;
import com.framework.boundaries.PasswordBoundary;
import com.framework.boundaries.UserBoundary;
import com.framework.boundaries.UserLoginDetails;
import com.framework.communication.EmailService;
import com.framework.constants.DataKeyValue;
import com.framework.constants.EventType;
import com.framework.constants.RegisteredAccount;
import com.framework.constants.ServerDefaults;
import com.framework.constants.UserData;
import com.framework.constants.UserRole;
import com.framework.data.DataEntity;
import com.framework.data.PasswordEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.DataDao;
import com.framework.data.dao.PasswordDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.AlreadyExistingException;
import com.framework.exceptions.InvalidMailException;
import com.framework.exceptions.NotFoundException;
import com.framework.exceptions.UnauthorizedRequest;
import com.framework.exceptions.WeakPasswordException;
import com.framework.logic.UserService;
import com.framework.logic.converters.DataEntityConverterImplementation;
import com.framework.logic.converters.PasswordEntityConverterImlementation;
import com.framework.logic.converters.UserEntityConverterImplementation;
import com.framework.security.services.DictionaryAttackPrevention;
import com.framework.security.services.PasswordUtils;
import com.framework.security.services.ResetPasswordCachingService;
import com.framework.security.sessions.SessionAttributes;
import com.framework.utilities.Validations;

@Service
public class UserServiceJpa implements UserService {
	private UserDao userDao;
	private PasswordDao passwordDao;
	private DataDao dataDao;
	private EventServiceJpa eventServiceJpa;
	private UserEntityConverterImplementation ueConverter;
	private PasswordEntityConverterImlementation peConverter;
	private DataEntityConverterImplementation deConverter;
	private PasswordEncoder passwordEncoder;
	private PasswordUtils passwordRules;
	private ResetPasswordCachingService otkService;
	private Validations utils;
	private SessionAttributes session;
	private DictionaryAttackPrevention dap;
	private EmailService emailService;
	
	public UserServiceJpa() {
	}

	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Autowired
	public void setDataDao(DataDao dataDao) {
		this.dataDao = dataDao;
	}
	
	@Autowired
	public void setEventServiceJpa(EventServiceJpa eventServiceJpa) {
		this.eventServiceJpa = eventServiceJpa;
	}

	@Autowired
	public void setOtkService(ResetPasswordCachingService otkService) {
		this.otkService = otkService;
	}

	@Autowired
	public void setPasswordUtils(PasswordUtils passwordUtils) {
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
	public void setDeConverter(DataEntityConverterImplementation deConverter) {
		this.deConverter = deConverter;
	}
	
	@Autowired
	public void setUtils(Validations utils) {
		this.utils = utils;
	}

	@Autowired
	public void setSession(SessionAttributes session) {
		this.session = session;
	}

	@Autowired
	public void setDap(DictionaryAttackPrevention dap) {
		this.dap = dap;
	}

	@Autowired
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	@Override
	@Transactional
	public UserBoundary register(UserBoundary user) {
		utils.assertNull(user);
		utils.assertNull(user.getUserId());
		utils.assertNull(user.getName());
		utils.assertEmptyString(user.getUserId().getUID());
		utils.assertNull(user.getUserId().getPasswordBoundary());
		utils.assertEmptyString(user.getUserId().getPasswordBoundary().getPassword());

		/**
		 * Basic account details
		 * **/
		Optional<UserEntity> exitingUser = userDao.findById(user.getUserId().getUID());
		if (exitingUser.isPresent())
			throw new AlreadyExistingException("uid already in the database");
		user.getUserId().getPasswordBoundary().setCreationTime(new Date());	
		user.setRole(UserRole.PLAYER);
		user.setActive(true);
		
		/**
		 * Password setup
		 * **/
		if (!passwordRules.checkMail(user.getUserId().getUID()))
			throw new InvalidMailException("Invalid mail");
		if (dap.isPassInDictionary(user.getUserId().getPasswordBoundary().getPassword()))
			throw new WeakPasswordException("Password appeared in a leaked password database, choose another one");
		// Hash the password before converting to entity
		if (!passwordRules.checkPassword(user.getUserId().getPasswordBoundary().getPassword()))
			throw new WeakPasswordException("Password does not meet the minimum requirenments");
		String hashedPass = passwordEncoder.encode(user.getUserId().getPasswordBoundary().getPassword());
		user.getUserId().getPasswordBoundary().setPassword(hashedPass);

		/**
		 * Configuration setup
		 * **/
		DataBoundary configurationBoundary = new DataBoundary();
		configurationBoundary.setDataId(UserData.CONFIGURATION.name() + "@" + user.getUserId().getUID());
		configurationBoundary.setDataType(UserData.CONFIGURATION);
		configurationBoundary.setCreatedTimestamp(new Date());
		Map<String, Object> attributes = new HashMap<>();
		attributes.put(DataKeyValue.REGISTERED_ACCOUNT.name(), RegisteredAccount.FREE_ACCOUNT.name());
		attributes.put(DataKeyValue.MAX_DISK_QUOTA.name(), ServerDefaults.MAX_STORAGE_FREE_ACCOUNT);
		attributes.put(DataKeyValue.CURRENT_DISK_QUOTA.name(), 0);
		configurationBoundary.setDataAttributes(attributes);
		
		/**
		 * Save to DB
		 * **/
		UserEntity ue = ueConverter.fromBoundary(user);
		DataEntity configurationEntity = this.deConverter.fromBoundary(configurationBoundary);
		PasswordEntity pe = peConverter.fromBoundary(user.getUserId().getPasswordBoundary());	

		ue.addDataToUser(configurationEntity);		
		ue.addPassword(pe);
		
		dataDao.save(configurationEntity);
		userDao.save(ue);
		return user;
	}

	@Override
	public UserBoundary updateUser(UserBoundary update) {
		// TODO get currently logged-in password details
		String authenticatedUser = session.retrieveAuthenticatedUsername();

		boolean dirty = false;
		utils.assertOwnership(authenticatedUser, update.getUserId().getUID());

		// Find the corresponding user in the database
		UserEntity existingEntity = userDao.findById(update.getUserId().getUID())
				.orElseThrow(() -> new NotFoundException("User not found"));

		PasswordBoundary updatedPassBoundary = update.getUserId().getPasswordBoundary();
		String newPassword = updatedPassBoundary.getPassword();

		// The device requests to suspend activity, this will still allow the parent to
		// view its data but will stop the device's future monitoring..
		// Currently not allowing user account deactivation but only devices
		if (existingEntity.getRole().equals(UserRole.DEVICE.name())) {
			if (update.getActive() != null) {
				existingEntity.setActive(update.getActive());
				dirty = true;
			}
		}

		if (update.getName() != null) {
			existingEntity.setName(update.getName());
			dirty = true;
		}
		// The user requests to change his password
		if (updatedPassBoundary != null && newPassword != null) {
			// TODO Check authentication details / the user is required to enter his old
			// password in the optional input
			if (!passwordEncoder.matches(update.getUserId().getPasswordBoundary().getOptionalPassword(),
					existingEntity.getActivePasswordEntity().getPassword()))
				throw new UnauthorizedRequest("Failed to verify old password");
			if (!passwordRules.isPasswordInHistory(newPassword, existingEntity.getPasswords())) {
				// Check if the new password is a valid password
				if (dap.isPassInDictionary(newPassword))
					throw new WeakPasswordException(
							"Password appeared in a leaked password database, choose another one");
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
				} else {
					throw new WeakPasswordException("Password does not meet the minimum requirenments");
				}
			} else
				throw new AlreadyExistingException("Password was already used in the past");
		}
		if (dirty)
			userDao.save(existingEntity);
		return ueConverter.toBoundary(existingEntity);
	}

	@Override
	public void sendOtkViaEmail(String userEmail) {
		if (!passwordRules.checkMail(userEmail))
			throw new InvalidMailException("Invalid mail");
		try {
			boolean isFirstTime = true;
			if (this.otkService.hasKey(userEmail))
				isFirstTime = false;
			String otp = this.otkService.getOTK(userEmail);
			if (isFirstTime)
				emailService.sendEmail(userEmail, otp, "Blackeagle - Reset password verification");
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void resetPassword(PasswordBoundary passDetails) {
		// Only authenticated with a RES_AUTH Token
		String authenticatedUser = session.retrieveAuthenticatedUsername();

		// Find the corresponding user in the database
		UserEntity existingEntity = userDao.findById(authenticatedUser)
				.orElseThrow(() -> new NotFoundException("User not found"));

		if (dap.isPassInDictionary(passDetails.getPassword()))
			throw new WeakPasswordException("Password appeared in a leaked password database, choose another one");
		// Hash the password before converting to entity
		if (!passwordRules.checkPassword(passDetails.getPassword()))
			throw new WeakPasswordException("Password does not meet the minimum requirenments");

		String hashedPass = passwordEncoder.encode(passDetails.getPassword());
		passDetails.setPassword(hashedPass);
		passDetails.setCreationTime(new Date());
		// Convert and save the password entity
		PasswordEntity pe = peConverter.fromBoundary(passDetails);
		existingEntity.addPassword(pe);

		eventServiceJpa.createEvent(authenticatedUser, EventType.PASSWORD_RESET);
		userDao.save(existingEntity);
	}

	@Override
	public UserBoundary deleteAccount(UserLoginDetails loginDetails) {
		String authenticatedUser = session.retrieveAuthenticatedUsername();

		Optional<UserEntity> existingUser = userDao.findById(authenticatedUser);
		if (!existingUser.isPresent())
			throw new NotFoundException("User does not exists in the database");

		// Check if the re-entered password matches the real hashed password
		if (!passwordEncoder.matches(loginDetails.getPassword(),
				existingUser.get().getActivePasswordEntity().getPassword()))
			throw new UnauthorizedRequest("Wrong password");

		// user validated successfully, proceed to delete his account
		for (UserEntity device : existingUser.get().getDevices()) {
			try {
				FileUtils.deleteDirectory(new File(ServerDefaults.SERVER_USER_DATA_PATH + "/" + device.getId()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		userDao.deleteById(authenticatedUser);

		return ueConverter.toBoundary(existingUser.get());
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserEntity> exitingUser = userDao.findById(username);
		if (!exitingUser.isPresent() || !exitingUser.get().isActive())
			throw new UsernameNotFoundException("User does not exists in the database");
		UserEntity ue = exitingUser.get();

		ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>();

		// Only one role per user, set current saved role
		grantedAuthorities.add(new SimpleGrantedAuthority(ue.getRole()));

		return new org.springframework.security.core.userdetails.User(ue.getId(),
				ue.getActivePasswordEntity().getPassword(), grantedAuthorities);
	}

}
