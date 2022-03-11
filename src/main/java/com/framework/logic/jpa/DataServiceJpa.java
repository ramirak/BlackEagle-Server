package com.framework.logic.jpa;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
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
import com.framework.data.DataEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.DataDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.AlreadyExistingException;
import com.framework.exceptions.NotFoundException;
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
	public DataBoundary addData(String ownerId, DataBoundary newData, MultipartFile file) {
		validations.assertNull(ownerId);
		validations.assertNull(newData);
		validations.assertNull(newData.getDataType());
		validations.assertValidDataType(newData.getDataType().name());

		String authenticatedUser = session.retrieveAuthenticatedUsername();
		UserEntity existingOwner = userDao.findById(ownerId)
				.orElseThrow(() -> new NotFoundException("User not found: " + ownerId));

		if (!ownerId.equals(authenticatedUser))
			validations.assertOwnership(authenticatedUser, existingOwner.getDeviceOwner().getUid());

		String newUID = UUID.randomUUID().toString();

		Optional<DataEntity> existingDataOptional = dataDao.findById(newUID);
		// We would like to prevent the small chance of two equal UUID
		while (existingDataOptional.isPresent())
			newUID = UUID.randomUUID().toString();

		newData.setDataId(newUID);
		newData.setCreatedTimestamp(new Date());
		if (newData.getDataType() == UserData.REQUEST) {
			newData.getDataAttributes().put(DataKeyValue.REQUEST_STATE.name(), DataKeyValue.REQUEST_READY.name());
			// Check if the request type is valid
			if (newData.getDataAttributes().containsKey(DataKeyValue.REQUEST_TYPE.name())) {
				validations.assertValidDataType(
						newData.getDataAttributes().get(DataKeyValue.REQUEST_TYPE.name()).toString());
			}

			// Get all requests for current user
			List<DataEntity> allRequests = this.dataDao.findAllByDataTypeAndDataOwnerUid(UserData.REQUEST.name(),
					ownerId);

			// If the request already exists, do not add it..
			for (DataEntity dataEntity : allRequests) {
				if (jsConverter.JSONToMap(dataEntity.getDataAttributes()).get(DataKeyValue.REQUEST_TYPE.name())
						.equals(newData.getDataAttributes().get(DataKeyValue.REQUEST_TYPE.name()).toString()))
					throw new AlreadyExistingException("There is an already pending operation of the same type");
			}

		}
		if (file != null) {
			newData.getDataAttributes().put(DataKeyValue.ATTACHMENT.name(), Boolean.TRUE);
			try {
				userFiles.saveUploadedFile(file,
						ServerDefaults.SERVER_USER_DATA_PATH + "/" + existingOwner.getUid() + "/", newUID);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			newData.getDataAttributes().put(DataKeyValue.ATTACHMENT.name(), Boolean.FALSE);
		DataEntity dataEntity = this.deConverter.fromBoundary(newData);
		existingOwner.addDataToUser(dataEntity);

		this.dataDao.save(dataEntity);
		this.userDao.save(existingOwner);
		return this.deConverter.toBoundary(dataEntity);

	}

	@Override
	public DataBoundary updateData(DataBoundary update) {
		validations.assertNull(update);
		validations.assertNull(update.getDataId());

		String authenticatedUser = session.retrieveAuthenticatedUsername();

		Optional<DataEntity> existingData = dataDao.findById(update.getDataId());
		if (existingData.isPresent()) {
			DataEntity dataEntity = existingData.get();
			validations.assertOwnership(authenticatedUser, dataEntity.getDataOwner().getUid());
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

		validations.assertOwnership(existingDevice.getDeviceOwner().getUid(), authenticatedUser);

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

		validations.assertOwnership(existingDevice.getDeviceOwner().getUid(), authenticatedUser);
		this.dataDao.deleteAll(existingDevice.getUserData());
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

			validations.assertOwnership(authenticatedUser, deviceEntity.getDeviceOwner().getUid());
		}
		DataEntity de = dataDao.findByDataIdAndDataOwnerUid(dataId, uid);
		DataBoundary db = deConverter.toBoundary(de);

		if (db.getDataAttributes().containsKey(DataKeyValue.ATTACHMENT.name())
				&& db.getDataAttributes().get(DataKeyValue.ATTACHMENT.name()) == Boolean.TRUE) {
			String path = ServerDefaults.SERVER_USER_DATA_PATH + "/" + de.getDataOwner().getUid() + "/";
			Path p = FileSystems.getDefault().getPath(path, dataId);
			try {
				byte[] fileData = Files.readAllBytes(p);
				db.getDataAttributes().put(DataKeyValue.DATA.name(), fileData);
			} catch (IOException e) {
				e.printStackTrace();
			}
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

			validations.assertOwnership(authenticatedUser, deviceEntity.getDeviceOwner().getUid());
		}
		return this.dataDao
				.findAllByDataTypeAndDataOwnerUid(type.name(), uid,
						PageRequest.of(page, size, Direction.DESC, "createdTimestamp"))
				.stream().map(this.deConverter::toBoundary).collect(Collectors.toList());
	}

}