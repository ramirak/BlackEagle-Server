package com.framework.logic.jpa;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.framework.boundaries.PasswordBoundary;
import com.framework.boundaries.UserBoundary;
import com.framework.boundaries.UserIdBoundary;
import com.framework.constants.ServerDefaults;
import com.framework.constants.UserRole;
import com.framework.data.PasswordEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.PasswordDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.ForbiddenException;
import com.framework.exceptions.LimitExceededException;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.DeviceService;
import com.framework.logic.converters.PasswordEntityConverterImlementation;
import com.framework.logic.converters.UserEntityConverterImplementation;
import com.framework.security.services.EncryptionUtils;
import com.framework.security.sessions.SessionAttributes;
import com.framework.utilities.Validations;

@Service
public class DeviceServiceJpa implements DeviceService {
	private UserDao userDao;
	private PasswordDao passwordDao;
	private UserEntityConverterImplementation ueConverter;
	private PasswordEntityConverterImlementation peConverter;
	private PasswordEncoder passwordEncoder;
	private Validations utils;
	private SessionAttributes session;

	public DeviceServiceJpa() {
	}

	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Autowired
	public void setPasswordDao(PasswordDao passwordDao) {
		this.passwordDao = passwordDao;
	}

	@Autowired
	public void setUeConverter(UserEntityConverterImplementation ueConverter) {
		this.ueConverter = ueConverter;
	}

	@Autowired
	public void setPeConverter(PasswordEntityConverterImlementation peConverter) {
		this.peConverter = peConverter;
	}

	@Autowired
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Autowired
	public void setUtils(Validations utils) {
		this.utils = utils;
	}

	@Autowired
	public void setSession(SessionAttributes session) {
		this.session = session;
	}

	@Override
	public UserBoundary addDevice(UserBoundary device) {
		utils.assertNull(device);
		utils.assertNull(device.getName());

		String authenticatedUser = session.retrieveAuthenticatedUsername();

		UserEntity existingUser = userDao.findById(authenticatedUser).get();

		if (existingUser.getDeviceCount() >= ServerDefaults.MAX_NUM_DEVICES)
			throw new LimitExceededException("Device count exceeded");

		String newUID = UUID.randomUUID().toString();

		Optional<UserEntity> exitingDeviceOptional = userDao.findById(newUID);
		// We would like to prevent the zero chance of two equal UUID
		while (exitingDeviceOptional.isPresent())
			newUID = UUID.randomUUID().toString();

		device.setRole(UserRole.DEVICE);
		device.setActive(true);
		PasswordBoundary passBoundary = new PasswordBoundary();
		passBoundary.setActive(true);
		passBoundary.setCreationTime(new Date());

		String newKey;
		// TODO: check this function
		try {
			newKey = EncryptionUtils.convertSecretKeyToString(EncryptionUtils.generateKey(256));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		passBoundary.setPassword(passwordEncoder.encode(newKey));
		device.setUserId(new UserIdBoundary(newUID, passBoundary));
		UserEntity deviceEntity = this.ueConverter.fromBoundary(device);
		PasswordEntity passEntity = this.peConverter.fromBoundary(passBoundary);

		existingUser.addDeviceToUser(deviceEntity);
		deviceEntity.addPassword(passEntity);
		passwordDao.save(passEntity);
		userDao.save(deviceEntity);
		userDao.save(existingUser);
		// Return a boundary with original unhashed password
		deviceEntity.getActivePasswordEntity().setPassword(newKey);
		return this.ueConverter.toBoundary(deviceEntity);
	}

	@Override
	public UserBoundary updateDevice(UserBoundary update) {
		utils.assertNull(update);
		utils.assertNull(update.getUserId());

		boolean dirty = false;
		String authenticatedUser = session.retrieveAuthenticatedUsername();

		Optional<UserEntity> existingDevice = this.userDao.findById(update.getUserId().getUID());
		if (!existingDevice.isPresent())
			throw new NotFoundException("Could not find device by id " + update.getUserId().getUID());

		UserEntity deviceOwner = existingDevice.get().getDeviceOwner();

		utils.assertOwnership(authenticatedUser, deviceOwner.getUid());

		UserEntity deviceEntity = existingDevice.get();
		if (update.getName() != null) {
			deviceEntity.setName(update.getName());
			dirty = true;
		}
		if (dirty)
			deviceEntity = this.userDao.save(deviceEntity);
		return this.ueConverter.toBoundary(deviceEntity);
	}

	@Override
	public UserBoundary deleteDevice(String deviceId) {
		String authenticatedUser = session.retrieveAuthenticatedUsername();

		Optional<UserEntity> existingDevice = this.userDao.findById(deviceId);
		if (!existingDevice.isPresent())
			throw new NotFoundException("Could not find device by id " + deviceId);

		UserEntity deviceEntity = existingDevice.get();
		UserEntity deviceOwner = deviceEntity.getDeviceOwner();
		utils.assertOwnership(authenticatedUser, deviceOwner.getUid());

		// TODO: delete data
		// --------------------------------------------------------------------------------
		deviceOwner.setDeviceCount(deviceOwner.getDeviceCount() - 1);
		this.userDao.delete(deviceEntity);

		return ueConverter.toBoundary(deviceEntity);
	}

	@Override
	public UserBoundary getSpecificDevice(String deviceId) {
		utils.assertNull(deviceId);
		String authenticatedUser = session.retrieveAuthenticatedUsername();

		Optional<UserEntity> existingDevice = this.userDao.findById(deviceId);
		if (!existingDevice.isPresent())
			throw new NotFoundException("Could not find device by id " + deviceId);

		UserEntity deviceOwner = existingDevice.get().getDeviceOwner();
		// check if device owned by the authenticated user
		utils.assertOwnership(deviceOwner.getUid(), authenticatedUser);

		return this.ueConverter.toBoundary(userDao
				.findByActiveAndUidAndRoleAndDeviceOwnerUid(true, deviceId, UserRole.DEVICE, authenticatedUser).get());
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBoundary> getAllDevices(int page, int size) {
		String authenticatedUser = session.retrieveAuthenticatedUsername();

		UserEntity existingUser = userDao.findById(authenticatedUser).get();

		if (existingUser.getRole() == UserRole.PLAYER.name()) {
			return this.userDao
					.findAllByActiveAndRoleAndDeviceOwnerUid(true, UserRole.DEVICE, authenticatedUser,
							PageRequest.of(page, size, Direction.DESC, "name"))
					.stream().map(this.ueConverter::toBoundary).collect(Collectors.toList());
		} else
			throw new ForbiddenException();
	}
}
