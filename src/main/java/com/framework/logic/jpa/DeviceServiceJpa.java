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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.framework.boundaries.PasswordBoundary;
import com.framework.boundaries.UserBoundary;
import com.framework.boundaries.UserIdBoundary;
import com.framework.constants.ServerDefaults;
import com.framework.data.PasswordEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.PasswordDao;
import com.framework.data.dao.UserDao;
import com.framework.datatypes.UserRole;
import com.framework.exceptions.LimitExceededException;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.DeviceService;
import com.framework.logic.converters.PasswordEntityConverterImlementation;
import com.framework.logic.converters.UserEntityConverterImplementation;
import com.framework.security.services.EncryptionUtils;
import com.framework.utilities.Utils;

@Service
public class DeviceServiceJpa implements DeviceService {
	private UserEntityConverterImplementation ueConverter;
	private PasswordEntityConverterImlementation peConverter;
	private PasswordEncoder passwordEncoder;
	private PasswordDao passwordDao;
	private UserDao userDao;
	private Utils utils;

	public DeviceServiceJpa() {
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
	public void setPeConverter(PasswordEntityConverterImlementation peConverter) {
		this.peConverter = peConverter;
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
	public void setUtils(Utils utils) {
		this.utils = utils;
	}

	@Override
	public UserBoundary addDevice(UserBoundary device) {
		utils.assertNull(device);

		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.getUsername();
		UserEntity existingUser = userDao.findById(authenticatedUser)
				.orElseThrow(() -> new NotFoundException("User not found"));

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
			newKey = EncryptionUtils.generateKey(256).toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		passBoundary.setPassword(passwordEncoder.encode(newKey));
		device.setUserId(new UserIdBoundary(newUID, passBoundary));
		UserEntity deviceEntity = this.ueConverter.fromBoundary(device);
		PasswordEntity passEntity = this.peConverter.fromBoundary(passBoundary);

		existingUser.addDeviceToUser(deviceEntity);
		existingUser.addPassword(passEntity);
		passwordDao.save(passEntity);
		userDao.save(deviceEntity);
		this.userDao.save(existingUser);
		// Return a boundary with original unhashed password
		deviceEntity.getActivePasswordEntity().setPassword(newKey);
		return this.ueConverter.toBoundary(deviceEntity);
	}

	@Override
	public UserBoundary updateDevice(UserBoundary update) {
		utils.assertNull(update);
		utils.assertNull(update.getUserId());

		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.getUsername();
		utils.assertOwnership(authenticatedUser, update.getUserId().getUID());

		Optional<UserEntity> existingDevice = userDao.findById(update.getUserId().getUID());
		if (existingDevice.isPresent()) {
			UserEntity deviceEntity = existingDevice.get();

			// TODO: What we need to update?!

			deviceEntity = this.userDao.save(deviceEntity);
			return this.ueConverter.toBoundary(deviceEntity);
		} else {
			throw new NotFoundException("Could not find device in the database");
		}
	}

	@Override
	public UserBoundary deleteDevice(String deviceId) {
		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.getUsername();
		Optional<UserEntity> existingDevice = this.userDao.findById(deviceId);
		if (!existingDevice.isPresent())
			throw new NotFoundException("Could not find device by id " + deviceId);

		utils.assertOwnership(deviceId, authenticatedUser);

		UserEntity deviceEntity = existingDevice.get();
		this.userDao.delete(deviceEntity);
		return ueConverter.toBoundary(deviceEntity);
	}

	@Override
	public UserBoundary getSpecificDevice(String deviceId) {
		utils.assertNull(deviceId);

		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.getUsername();
		Optional<UserEntity> existingDevice = this.userDao.findById(deviceId);
		if (!existingDevice.isPresent())
			throw new NotFoundException("Could not find device by id " + deviceId);

		// check if device owned by the authenticated user
		utils.assertOwnership(deviceId, authenticatedUser);

		UserEntity deviceEntity = existingDevice.get();
		return this.ueConverter.toBoundary(deviceEntity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBoundary> getAllDevices(String userId, int page, int size) {
		utils.assertNull(userId);

		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.getUsername();
		UserEntity existingUser = userDao.findById(authenticatedUser)
				.orElseThrow(() -> new NotFoundException("User not found: " + userId));

		if (existingUser.getRole() == UserRole.PLAYER.name()) {
			return this.userDao.findAllByActive(true, PageRequest.of(page, size, Direction.DESC, "XXXXX", "deviceId"))
					.stream().map(this.ueConverter::toBoundary).collect(Collectors.toList());
		}
		return this.userDao.findAll(PageRequest.of(page, size, Direction.DESC, "XXXXX", "deviceId")).getContent()
				.stream().map(this.ueConverter::toBoundary).collect(Collectors.toList());
	}
}
