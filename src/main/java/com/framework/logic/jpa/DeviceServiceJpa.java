package com.framework.logic.jpa;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.framework.boundaries.DataBoundary;
import com.framework.boundaries.PasswordBoundary;
import com.framework.boundaries.UserBoundary;
import com.framework.boundaries.UserIdBoundary;
import com.framework.constants.DataKeyValue;
import com.framework.constants.EventType;
import com.framework.constants.FilterType;
import com.framework.constants.ServerDefaults;
import com.framework.constants.UserData;
import com.framework.constants.UserRole;
import com.framework.data.DataEntity;
import com.framework.data.PasswordEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.DataDao;
import com.framework.data.dao.PasswordDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.ForbiddenException;
import com.framework.exceptions.LimitExceededException;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.DeviceService;
import com.framework.logic.converters.DataEntityConverterImplementation;
import com.framework.logic.converters.JsonConverterImplementation;
import com.framework.logic.converters.PasswordEntityConverterImlementation;
import com.framework.logic.converters.UserEntityConverterImplementation;
import com.framework.security.services.PasswordUtils;
import com.framework.security.sessions.SessionAttributes;
import com.framework.utilities.Validations;

@Service
public class DeviceServiceJpa implements DeviceService {
	private UserDao userDao;
	private PasswordDao passwordDao;
	private DataDao dataDao;
	private UserEntityConverterImplementation ueConverter;
	private PasswordEntityConverterImlementation peConverter;
	private DataEntityConverterImplementation deConverter;
	private JsonConverterImplementation jsConverter;
	private PasswordEncoder passwordEncoder;
	private Validations utils;
	private SessionAttributes session;
	private PasswordUtils passUtils;
	private EventServiceJpa eventServiceJpa;

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
	public void setDataDao(DataDao dataDao) {
		this.dataDao = dataDao;
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
	public void setDeConverter(DataEntityConverterImplementation deConverter) {
		this.deConverter = deConverter;
	}

	@Autowired
	public void setJsConverter(JsonConverterImplementation jsConverter) {
		this.jsConverter = jsConverter;
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

	@Autowired
	public void setPassUtils(PasswordUtils passUtils) {
		this.passUtils = passUtils;
	}

	@Autowired
	public void setEventServiceJpa(EventServiceJpa eventServiceJpa) {
		this.eventServiceJpa = eventServiceJpa;
	}

	@Override
	public UserBoundary addDevice(UserBoundary device) {
		utils.assertNull(device);
		utils.assertNull(device.getName());
		utils.assertEmptyString(device.getName());

		String authenticatedUser = session.retrieveAuthenticatedUsername();
		UserEntity existingUser = userDao.findById(authenticatedUser).get();

		Optional<DataEntity> userConfig = dataDao.findById(UserData.CONFIGURATION.name() + "@" + existingUser.getId());
		Map<String, Object> configAttr = jsConverter.JSONToMap(userConfig.get().getDataAttributes());
		int maxDevices = (Integer) configAttr.get(DataKeyValue.MAX_ALLOWED_DEVICES.name());
		int currentNumDevices = (Integer) configAttr.get(DataKeyValue.CURRENT_NUM_DEVICES.name()) + 1;

		configAttr.put(DataKeyValue.CURRENT_NUM_DEVICES.name(), currentNumDevices);
		userConfig.get().setDataAttributes(jsConverter.mapToJSON(configAttr));

		if (currentNumDevices > maxDevices)
			throw new LimitExceededException("Device count exceeded");

		String newUID = UUID.randomUUID().toString();

		Optional<UserEntity> exitingDeviceOptional = userDao.findById(newUID);
		// We would like to prevent the zero chance of two equal UUID
		while (exitingDeviceOptional.isPresent())
			newUID = UUID.randomUUID().toString();

		device.setRole(UserRole.DEVICE);
		PasswordBoundary passBoundary = new PasswordBoundary();
		passBoundary.setActive(true);
		passBoundary.setCreationTime(new Date());

		String newKey = passUtils.generatePassword();
		passBoundary.setPassword(passwordEncoder.encode(newKey));
		device.setUserId(new UserIdBoundary(newUID, passBoundary));

		DataBoundary dataBoundary = new DataBoundary();
		dataBoundary.setDataId(UserData.CONFIGURATION.name() + "@" + newUID);
		dataBoundary.setDataType(UserData.CONFIGURATION);
		dataBoundary.setCreatedTimestamp(new Date());
		Map<String, Object> attributes = new HashMap<>();
		attributes.put(DataKeyValue.IS_ACTIVE.name(), "true");
		attributes.put(FilterType.FAKENEWS.name(), "false");
		attributes.put(FilterType.GAMBLING.name(), "false");
		attributes.put(FilterType.PORN.name(), "false");
		attributes.put(FilterType.SOCIAL.name(), "false");
		attributes.put(DataKeyValue.ADDITIONAL_SITES.name(), jsConverter.setToJSON(new HashSet<Object>()));
		attributes.put(DataKeyValue.FILE_SIZE.name(), "0");
		dataBoundary.setDataAttributes(attributes);

		UserEntity deviceEntity = this.ueConverter.fromBoundary(device);
		PasswordEntity passEntity = this.peConverter.fromBoundary(passBoundary);
		DataEntity dataEntity = this.deConverter.fromBoundary(dataBoundary);

		existingUser.addDeviceToUser(deviceEntity);
		deviceEntity.addPassword(passEntity);
		deviceEntity.addDataToUser(dataEntity);
		passwordDao.save(passEntity);
		dataDao.save(userConfig.get());
		dataDao.save(dataEntity);
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

		utils.assertOwnership(authenticatedUser, deviceOwner.getId());

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
		utils.assertNull(deviceId);

		String authenticatedUser = session.retrieveAuthenticatedUsername();

		Optional<UserEntity> existingDevice = this.userDao.findById(deviceId);
		if (!existingDevice.isPresent())
			throw new NotFoundException("Could not find device by id " + deviceId);

		UserEntity deviceEntity = existingDevice.get();
		UserEntity deviceOwner = deviceEntity.getDeviceOwner();
		utils.assertOwnership(authenticatedUser, deviceOwner.getId());

		// Sum up all the sizes
		double sizeSum = dataDao.findAllByDataOwnerUid(deviceId).stream()
				.mapToDouble(data -> Double.parseDouble(
						(String) jsConverter.JSONToMap(data.getDataAttributes()).get(DataKeyValue.FILE_SIZE.name())))
				.sum();
		try {
			FileUtils.deleteDirectory(new File(ServerDefaults.SERVER_USER_DATA_PATH + "/" + deviceId));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Optional<DataEntity> userConfig = dataDao.findById(UserData.CONFIGURATION.name() + "@" + deviceOwner.getId());
		Map<String, Object> configAttr = jsConverter.JSONToMap(userConfig.get().getDataAttributes());
		int currentNumDevices = (Integer) configAttr.get(DataKeyValue.CURRENT_NUM_DEVICES.name()) - 1;
		double currentQuota = Double.parseDouble((String) configAttr.get(DataKeyValue.CURRENT_DISK_QUOTA.name()));
		currentQuota -= sizeSum;
		if (currentQuota < 0)
			currentQuota = 0;
		configAttr.put(DataKeyValue.CURRENT_DISK_QUOTA.name(), Double.toString(currentQuota));
		configAttr.put(DataKeyValue.CURRENT_NUM_DEVICES.name(), currentNumDevices);
		userConfig.get().setDataAttributes(jsConverter.mapToJSON(configAttr));

		this.dataDao.save(userConfig.get());
		this.userDao.delete(deviceEntity);

		eventServiceJpa.createEvent(authenticatedUser, EventType.DEVICE_DELETED);
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
		utils.assertOwnership(deviceOwner.getId(), authenticatedUser);

		return this.ueConverter.toBoundary(
				userDao.findByUidAndRoleAndDeviceOwnerUid(deviceId, UserRole.DEVICE.name(), authenticatedUser).get());
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBoundary> getAllDevices(int page, int size) {
		String authenticatedUser = session.retrieveAuthenticatedUsername();

		UserEntity existingUser = userDao.findById(authenticatedUser).get();

		if (existingUser.getRole().equals(UserRole.PLAYER.name())) {
			return this.userDao
					.findAllByRoleAndDeviceOwnerUid(UserRole.DEVICE.name(), authenticatedUser,
							PageRequest.of(page, size, Direction.DESC, "name"))
					.stream().map(this.ueConverter::toBoundary).collect(Collectors.toList());
		} else
			throw new ForbiddenException();
	}
}
