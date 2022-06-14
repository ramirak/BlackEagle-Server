package com.framework.logic.jpa;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.framework.boundaries.DataBoundary;
import com.framework.constants.DataKeyValue;
import com.framework.constants.FilterType;
import com.framework.constants.ServerDefaults;
import com.framework.constants.UserData;
import com.framework.constants.UserRole;
import com.framework.data.DataEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.DataDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.AlreadyExistingException;
import com.framework.exceptions.BadRequestException;
import com.framework.exceptions.LimitExceededException;
import com.framework.exceptions.NotFoundException;
import com.framework.exceptions.UnauthorizedRequest;
import com.framework.logic.DataService;
import com.framework.logic.converters.DataEntityConverterImplementation;
import com.framework.logic.converters.JsonConverter;
import com.framework.security.sessions.SessionAttributes;
import com.framework.utilities.UserFiles;
import com.framework.utilities.Validations;

@Service
public class DataServiceJpa implements DataService {
	private DataEntityConverterImplementation deConverter;
	private JsonConverter jsConverter;
	private DataDao dataDao;
	private UserDao userDao;
	private Validations validations;
	private SessionAttributes session;
	private UserFiles userFiles;
	private DataHelperService dhs;

	public DataServiceJpa() {
	}

	@Autowired
	public void setUserFiles(UserFiles userFiles) {
		this.userFiles = userFiles;
	}

	@Autowired
	public void setSession(SessionAttributes session) {
		this.session = session;
	}

	@Autowired
	public void setUeConverter(DataEntityConverterImplementation ueConverter) {
		this.deConverter = ueConverter;
	}

	@Autowired
	public void setJsConverter(JsonConverter jsConverter) {
		this.jsConverter = jsConverter;
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
	public void setValidations(Validations validations) {
		this.validations = validations;
	}

	@Autowired
	public void setDhs(DataHelperService dhs) {
		this.dhs = dhs;
	}

	@Override
	@Transactional
	public DataBoundary addData(String ownerId, DataBoundary newData) { // Each user can upload boundaries to himself or
																		// his devices
		validations.assertNull(ownerId);
		validations.assertNull(newData);
		validations.assertNull(newData.getDataType());
		validations.assertNull(newData.getDataAttributes());
		dhs.checkDataType(newData.getDataType().name());

		dhs.allowedTypes(newData.getDataType().name(), new UserData[] { UserData.REQUEST });

		String authenticatedUser = session.retrieveAuthenticatedUsername();
		UserEntity existingOwner = userDao.findById(ownerId)
				.orElseThrow(() -> new NotFoundException("User not found: " + ownerId));

		if (!ownerId.equals(authenticatedUser))
			validations.assertOwnership(authenticatedUser, existingOwner.getDeviceOwner().getId());

		String newUID = UUID.randomUUID().toString();

		dhs.checkRequest(existingOwner, newData);
		newUID = "REQUEST_" + newData.getDataAttributes().get(DataKeyValue.REQUEST_TYPE.name()).toString() + "@"
				+ ownerId;

		Optional<DataEntity> existingDataOptional = dataDao.findById(newUID);
		if (existingDataOptional.isPresent())
			throw new AlreadyExistingException();

		newData.setDataId(newUID);
		newData.setCreatedTimestamp(new Date());

		newData.getDataAttributes().put(DataKeyValue.ATTACHMENT.name(), Boolean.FALSE);
		newData.getDataAttributes().put(DataKeyValue.FILE_SIZE.name(), "0");

		DataEntity dataEntity = this.deConverter.fromBoundary(newData);
		existingOwner.addDataToUser(dataEntity);

		this.dataDao.save(dataEntity);
		this.userDao.save(existingOwner);
		return this.deConverter.toBoundary(dataEntity);
	}

	@Override
	@Transactional
	public DataBoundary addData(DataBoundary newData, MultipartFile file) { // Only devices can upload files to their
																			// accounts
		validations.assertNull(newData);
		validations.assertNull(newData.getDataType());
		dhs.checkDataType(newData.getDataType().name());

		dhs.notAllowedTypes(newData.getDataType().name(),
				new UserData[] { UserData.REQUEST, UserData.CONFIGURATION, UserData.NOTIFCATION });

		String authenticatedUser = session.retrieveAuthenticatedUsername();
		UserEntity existingOwner = userDao.findById(authenticatedUser)
				.orElseThrow(() -> new NotFoundException("User not found"));

		String newUID = UUID.randomUUID().toString();
		Optional<DataEntity> existingDataOptional = dataDao.findById(newUID);

		while (existingDataOptional.isPresent())
			newUID = UUID.randomUUID().toString();

		newData.setDataId(newUID);
		newData.setCreatedTimestamp(new Date());
		if (newData.getDataAttributes() == null)
			newData.setDataAttributes(new HashMap<>());
		newData.getDataAttributes().put(DataKeyValue.ATTACHMENT.name(), Boolean.TRUE);
		try {
			double size = dhs.getFileSize(file.getSize()); // Convert to MB
			newData.getDataAttributes().put(DataKeyValue.FILE_SIZE.name(), Double.toString(size));
			Optional<DataEntity> userConfig = dataDao
					.findById(UserData.CONFIGURATION.name() + "@" + existingOwner.getDeviceOwner().getId());
			Map<String, Object> configAttr = jsConverter.JSONToMap(userConfig.get().getDataAttributes());
			double currentQuota = Double.parseDouble((String) configAttr.get(DataKeyValue.CURRENT_DISK_QUOTA.name()));
			double maxQuota = Double.parseDouble((String) configAttr.get(DataKeyValue.MAX_DISK_QUOTA.name()));
			currentQuota += size;

			if (currentQuota > maxQuota)
				throw new LimitExceededException("User exceeded his account disk quota");

			configAttr.put(DataKeyValue.CURRENT_DISK_QUOTA.name(), Double.toString(currentQuota));
			userFiles.saveUploadedFile(file, ServerDefaults.SERVER_USER_DATA_PATH + "/" + existingOwner.getId() + "/",
					newUID, true); // Encrypted
			userConfig.get().setDataAttributes(jsConverter.mapToJSON(configAttr));
			this.dataDao.save(userConfig.get());
		} catch (IOException e) {
			e.printStackTrace();
		}
		DataEntity dataEntity = this.deConverter.fromBoundary(newData);
		existingOwner.addDataToUser(dataEntity);

		List<DataEntity> allRequests = dataDao.findAllByDataTypeAndDataOwnerUid(UserData.REQUEST.name(),
				authenticatedUser);
		for (DataEntity entity : allRequests) {
			// Look for corresponding request in the database and remove it..
			if (jsConverter.JSONToMap(entity.getDataAttributes()).get(DataKeyValue.REQUEST_TYPE.name()).toString()
					.equals(newData.getDataType().name()))
				this.dataDao.delete(entity);
		}
		this.dataDao.save(dataEntity);
		this.userDao.save(existingOwner);
		return this.deConverter.toBoundary(dataEntity);
	}

	@Override
	@Transactional
	public DataBoundary updateData(DataBoundary update) {
		validations.assertNull(update);
		validations.assertNull(update.getDataId());
		dhs.checkDataType(update.getDataType().name());

		String authenticatedUser = session.retrieveAuthenticatedUsername();

		Optional<DataEntity> existingData = dataDao.findById(update.getDataId());
		if (existingData.isPresent()) {
			DataEntity dataEntity = existingData.get();
			boolean isDevice = dataEntity.getDataOwner().getRole().equals(UserRole.DEVICE.name());
			if (isDevice)
				validations.assertOwnership(authenticatedUser, dataEntity.getDataOwner().getDeviceOwner().getId());
			else
				validations.assertOwnership(authenticatedUser, dataEntity.getDataOwner().getId());

			if (update.getDataAttributes() != null) {
				if (update.getDataType() == UserData.CONFIGURATION) {
					if (isDevice) {
						Set<Object> additionalSites = jsConverter.JSONToSet((String) jsConverter
								.JSONToMap(dataEntity.getDataAttributes()).get(DataKeyValue.ADDITIONAL_SITES.name()));
						String site = (String) update.getDataAttributes().get(DataKeyValue.ADDITIONAL_SITES.name());

						if (site != null && !site.isEmpty()) {
							site = dhs.CheckDomain(site);
							String operation = (String) update.getDataAttributes()
									.get(DataKeyValue.ADDITIONAL_SITES_OPERATION.name());
							if (operation.equals("ADD"))
								additionalSites.add(ServerDefaults.FILTER_REDIRECTION + " " + site);
							else if (operation.equals("REMOVE"))
								additionalSites.remove(ServerDefaults.FILTER_REDIRECTION + " " + site);
							else
								throw new BadRequestException("Unrecognized operation");
						}

						Map<String, Object> originalAttr = jsConverter.JSONToMap(dataEntity.getDataAttributes());

						String isActive = (String) update.getDataAttributes().get(DataKeyValue.IS_ACTIVE.name());
						String fakenews = (String) update.getDataAttributes().get(FilterType.FAKENEWS.name());
						String gambling = (String) update.getDataAttributes().get(FilterType.GAMBLING.name());
						String porn = (String) update.getDataAttributes().get(FilterType.PORN.name());
						String social = (String) update.getDataAttributes().get(FilterType.SOCIAL.name());

						if (isActive != null)
							originalAttr.put(DataKeyValue.IS_ACTIVE.name(), isActive);

						if (fakenews != null)
							originalAttr.put(FilterType.FAKENEWS.name(), fakenews);

						if (gambling != null)
							originalAttr.put(FilterType.GAMBLING.name(), gambling);

						if (porn != null)
							originalAttr.put(FilterType.PORN.name(), porn);

						if (social != null)
							originalAttr.put(FilterType.SOCIAL.name(), social);

						if (site != null && !site.isEmpty())
							originalAttr.put(DataKeyValue.ADDITIONAL_SITES.name(),
									jsConverter.setToJSON(additionalSites));

						dataEntity.setDataAttributes(jsConverter.mapToJSON(originalAttr));
					}
				} else
					throw new UnauthorizedRequest("Only configuration update is allowed right now");
			}
			dataEntity = this.dataDao.save(dataEntity);
			return this.deConverter.toBoundary(dataEntity);
		} else {
			throw new NotFoundException("Could not find requested data");
		}
	}

	@Override
	@Transactional
	public DataBoundary deleteData(String dataId) {
		validations.assertNull(dataId);

		String authenticatedUser = session.retrieveAuthenticatedUsername();

		Optional<DataEntity> existingData = this.dataDao.findById(dataId);
		if (!existingData.isPresent())
			throw new NotFoundException("Could not find data by id " + dataId);

		DataEntity dataEntity = existingData.get();
		dhs.notAllowedTypes(dataEntity.getDataType(), new UserData[] { UserData.CONFIGURATION });

		if (dataEntity.getDataOwner().getRole().equals(UserRole.DEVICE.name()))
			validations.assertOwnership(authenticatedUser, dataEntity.getDataOwner().getDeviceOwner().getId());
		else
			validations.assertOwnership(authenticatedUser, dataEntity.getDataOwner().getId());

		Map<String, Object> dataAttributes = jsConverter.JSONToMap(dataEntity.getDataAttributes());
		if (dataAttributes.get(DataKeyValue.ATTACHMENT.name()) == Boolean.TRUE) {
			String filePath = ServerDefaults.SERVER_USER_DATA_PATH + "/" + dataEntity.getDataOwner().getId() + "/"
					+ dataEntity.getId();
			File attachedFile = new File(filePath);

			Optional<DataEntity> userConfig = dataDao
					.findById(UserData.CONFIGURATION.name() + "@" + dataEntity.getDataOwner().getDeviceOwner().getId());
			Map<String, Object> configAttr = jsConverter.JSONToMap(userConfig.get().getDataAttributes());
			double currentQuota = Double.parseDouble((String) configAttr.get(DataKeyValue.CURRENT_DISK_QUOTA.name()));
			double attachedSize = Double.parseDouble((String) dataAttributes.get(DataKeyValue.FILE_SIZE.name()));
			currentQuota -= attachedSize;
			if (currentQuota < 0)
				currentQuota = 0;
			configAttr.put(DataKeyValue.CURRENT_DISK_QUOTA.name(), Double.toString(currentQuota));
			attachedFile.delete();
			userConfig.get().setDataAttributes(jsConverter.mapToJSON(configAttr));
			this.dataDao.save(userConfig.get());
		}

		this.dataDao.delete(dataEntity);
		return deConverter.toBoundary(dataEntity);
	}

	@Override
	@Transactional
	public void deleteAllData(String ownerId, String dataType) {
		validations.assertNull(ownerId);
		validations.assertNull(dataType);

		dhs.checkDataType(dataType);
		dhs.notAllowedTypes(dataType, new UserData[] { UserData.CONFIGURATION });

		String authenticatedUser = session.retrieveAuthenticatedUsername();

		UserEntity existingOwner = userDao.findById(ownerId).orElseThrow(() -> new NotFoundException("User not found"));

		if (!authenticatedUser.equals(ownerId))
			validations.assertOwnership(authenticatedUser, existingOwner.getDeviceOwner().getId());

		// Sum up all the sizes
		double sizeSum = dataDao.findAllByDataTypeAndDataOwnerUid(dataType, ownerId).stream()
				.mapToDouble(data -> Double.parseDouble(
						(String) jsConverter.JSONToMap(data.getDataAttributes()).get(DataKeyValue.FILE_SIZE.name())))
				.sum();

		// Delete all files
		dataDao.findAllByDataTypeAndDataOwnerUid(dataType, ownerId)
				.forEach(dataEntity -> new File(ServerDefaults.SERVER_USER_DATA_PATH + "/"
						+ dataEntity.getDataOwner().getId() + "/" + dataEntity.getId()).delete());

		Optional<DataEntity> userConfig = dataDao.findById(UserData.CONFIGURATION.name() + "@" + authenticatedUser);
		Map<String, Object> configAttr = jsConverter.JSONToMap(userConfig.get().getDataAttributes());
		double currentQuota = Double.parseDouble((String) configAttr.get(DataKeyValue.CURRENT_DISK_QUOTA.name()));
		currentQuota -= sizeSum;
		if (currentQuota < 0)
			currentQuota = 0;
		configAttr.put(DataKeyValue.CURRENT_DISK_QUOTA.name(), Double.toString(currentQuota));
		userConfig.get().setDataAttributes(jsConverter.mapToJSON(configAttr));
		this.dataDao.save(userConfig.get());

		dataDao.deleteAllByDataTypeAndDataOwnerUid(dataType, ownerId);
	}

	@Override
	@Transactional(readOnly = true)
	public DataBoundary getSpecificData(String uid, String dataId) {
		validations.assertNull(uid);
		validations.assertNull(dataId);

		String authenticatedUser = session.retrieveAuthenticatedUsername();

		if (!uid.equals(authenticatedUser)) {
			Optional<UserEntity> existingDevice = userDao.findById(uid);
			UserEntity deviceEntity;
			if (existingDevice.isPresent()) {
				deviceEntity = existingDevice.get();
			} else
				throw new NotFoundException("Device not found: " + uid);
			validations.assertOwnership(authenticatedUser, deviceEntity.getDeviceOwner().getId());
		}
		DataEntity de = dataDao.findByDataIdAndDataOwnerUid(dataId, uid);
		DataBoundary db = deConverter.toBoundary(de);

		if (db.getDataAttributes().containsKey(DataKeyValue.ATTACHMENT.name())
				&& db.getDataAttributes().get(DataKeyValue.ATTACHMENT.name()) == Boolean.TRUE) {
			String path = ServerDefaults.SERVER_USER_DATA_PATH + "/" + de.getDataOwner().getId() + "/";
			byte[] fileData = this.userFiles.getUploadedFile(path, dataId, true, true); // Decrypted + Base64 Encoded
			db.getDataAttributes().put(DataKeyValue.DATA.name(), new String(fileData, StandardCharsets.UTF_8)); // Bytes
																												// as
																												// ascii
																												// string
		}
		return db;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DataBoundary> getAllData(String uid, UserData type, int page, int size) {
		validations.assertNull(uid);
		String authenticatedUser = session.retrieveAuthenticatedUsername();

		if (!uid.equals(authenticatedUser)) {
			Optional<UserEntity> existingDevice = userDao.findById(uid);
			UserEntity deviceEntity;
			if (existingDevice.isPresent()) {
				deviceEntity = existingDevice.get();
			} else
				throw new NotFoundException("Device not found: " + uid);

			validations.assertOwnership(authenticatedUser, deviceEntity.getDeviceOwner().getId());
		}
		return this.dataDao
				.findAllByDataTypeAndDataOwnerUid(type.name(), uid,
						PageRequest.of(page, size, Direction.DESC, "createdTimestamp"))
				.stream().map(this.deConverter::toBoundary).collect(Collectors.toList());
	}
}