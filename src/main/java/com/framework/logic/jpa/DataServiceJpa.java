package com.framework.logic.jpa;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
import com.framework.constants.ServerDefaults;
import com.framework.constants.UserData;
import com.framework.constants.UserRole;
import com.framework.data.DataEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.DataDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.AlreadyExistingException;
import com.framework.exceptions.BadRequestException;
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

	@Override
	@Transactional
	public DataBoundary addData(String ownerId, DataBoundary newData) { // Each user can upload boundaries to himself or
																		// his devices
		validations.assertNull(ownerId);
		validations.assertNull(newData);
		validations.assertNull(newData.getDataType());
		validations.assertValidDataType(newData.getDataType().name());

		if (newData.getDataType() != UserData.REQUEST && newData.getDataType() != UserData.CONFIGURATION
				&& newData.getDataType() != UserData.NOTIFCATION)
			throw new BadRequestException("Call to wrong method");

		String authenticatedUser = session.retrieveAuthenticatedUsername();
		UserEntity existingOwner = userDao.findById(ownerId)
				.orElseThrow(() -> new NotFoundException("User not found: " + ownerId));

		if (!ownerId.equals(authenticatedUser))
			validations.assertOwnership(authenticatedUser, existingOwner.getDeviceOwner().getId());

		String newUID = UUID.randomUUID().toString();

		if (newData.getDataType() == UserData.REQUEST) {
			// Check if the owner is a device
			if (!existingOwner.getRole().equals(UserRole.DEVICE.name()))
				throw new UnauthorizedRequest("Only devices can have Data of type Request");
			// Check if the request type is valid
			if (newData.getDataAttributes().containsKey(DataKeyValue.REQUEST_TYPE.name())) {
				validations.assertValidDataType(
						newData.getDataAttributes().get(DataKeyValue.REQUEST_TYPE.name()).toString());
			}
			newUID = "REQUEST_" + newData.getDataAttributes().get(DataKeyValue.REQUEST_TYPE.name()) + "@" + ownerId;
		} else if (newData.getDataType() == UserData.CONFIGURATION) {
			newUID = newData.getDataType().name() + "@" + ownerId;
		} else {
			newUID = UUID.randomUUID().toString();
		}
		Optional<DataEntity> existingDataOptional = dataDao.findById(newUID);
		if(existingDataOptional.isPresent())
			throw new AlreadyExistingException();
		
		newData.setDataId(newUID);
		newData.setCreatedTimestamp(new Date());

		if (newData.getDataAttributes() == null)
			newData.setDataAttributes(new HashMap<>());
		newData.getDataAttributes().put(DataKeyValue.ATTACHMENT.name(), Boolean.FALSE);
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
		validations.assertValidDataType(newData.getDataType().name());
		if (newData.getDataType() == UserData.REQUEST || newData.getDataType() == UserData.CONFIGURATION
				|| newData.getDataType() == UserData.NOTIFCATION)
			throw new BadRequestException("Call to wrong method");

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
			long size = file.getSize();
			userFiles.saveUploadedFile(file, ServerDefaults.SERVER_USER_DATA_PATH + "/" + existingOwner.getId() + "/",
					newUID, true); // Encrypted
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

		String authenticatedUser = session.retrieveAuthenticatedUsername();

		Optional<DataEntity> existingData = dataDao.findById(update.getDataId());
		if (existingData.isPresent()) {
			DataEntity dataEntity = existingData.get();
			validations.assertOwnership(authenticatedUser, dataEntity.getDataOwner().getDeviceOwner().getId());
			if (update.getDataAttributes() != null) {
				dataEntity.setDataAttributes(jsConverter.mapToJSON(update.getDataAttributes()));
			}
			if (update.getDataType() != null) {
				dataEntity.setDataType(update.getDataType().name());
			}
			dataEntity = this.dataDao.save(dataEntity);
			return this.deConverter.toBoundary(dataEntity);
		} else {
			throw new NotFoundException("Could not find requested data");
		}
	}

	@Override
	@Transactional
	public DataBoundary deleteData(String deviceId, String dataId) {
		validations.assertNull(deviceId);
		validations.assertNull(dataId);

		String authenticatedUser = session.retrieveAuthenticatedUsername();
		UserEntity existingDevice = userDao.findById(deviceId)
				.orElseThrow(() -> new NotFoundException("Device not found: " + deviceId));

		validations.assertOwnership(existingDevice.getDeviceOwner().getId(), authenticatedUser);

		Optional<DataEntity> existingData = this.dataDao.findById(dataId);
		if (!existingData.isPresent())
			throw new NotFoundException("Could not find data by id " + dataId);
		DataEntity dataEntity = existingData.get();
		this.dataDao.delete(dataEntity);
		return deConverter.toBoundary(dataEntity);
	}

	@Override
	@Transactional
	public void deleteAllData(String deviceId) {
		validations.assertNull(deviceId);

		String authenticatedUser = session.retrieveAuthenticatedUsername();
		UserEntity existingDevice = userDao.findById(deviceId)
				.orElseThrow(() -> new NotFoundException("Device not found: " + deviceId));
		validations.assertOwnership(existingDevice.getDeviceOwner().getId(), authenticatedUser);
		this.dataDao.deleteAll();
	}

	@Override
	@Transactional(readOnly = true)
	public DataBoundary getSpecificData(String uid, String dataId) {
		validations.assertNull(uid);
		validations.assertNull(dataId);

		String authenticatedUser = session.retrieveAuthenticatedUsername();

		if (uid != authenticatedUser) {
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
			db.getDataAttributes().put(DataKeyValue.DATA.name(), new String(fileData, StandardCharsets.UTF_8)); // Bytes as ascii string
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