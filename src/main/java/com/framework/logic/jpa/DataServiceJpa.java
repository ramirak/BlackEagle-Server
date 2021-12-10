package com.framework.logic.jpa;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.framework.boundaries.DataBoundary;
import com.framework.data.DataEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.DataDao;
import com.framework.data.dao.UserDao;
import com.framework.datatypes.UserRole;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.DataService;
import com.framework.logic.converters.DataEntityConverterImplementation;
import com.framework.logic.converters.JsonConverter;
import com.framework.utilities.Utils;

@Service
public class DataServiceJpa implements DataService {
	private DataEntityConverterImplementation deConverter;
	private JsonConverter jsConverter;
	private DataDao dataDao;
	private UserDao userDao;
	private Utils utils;

	public DataServiceJpa() {
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

	public void setUtils(Utils utils) {
		this.utils = utils;
	}

	@Override
	// @Transactional
	public DataBoundary addData(String deviceId, DataBoundary newData) {
		utils.assertNull(deviceId);
		utils.assertNull(newData);
		utils.assertNull(newData.getDataType());
		utils.assertNull(newData.getDataOwner());

		// TODO: Encryption of Data

		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();		
		UserEntity existingDevice = userDao.findById(deviceId)
				.orElseThrow(() -> new NotFoundException("Device not found: " + deviceId));

		utils.assertOwnership(authenticatedUser, existingDevice.getDeviceOwner().getUid());

		String newUID = UUID.randomUUID().toString();

		Optional<DataEntity> existingDataOptional = dataDao.findById(newUID);
		// We would like to prevent the zero chance of two equal UUID
		while (existingDataOptional.isPresent())
			newUID = UUID.randomUUID().toString();

		newData.setDataId(newUID);
		newData.setCreatedTimestamp(new Date());
		DataEntity dataEntity = this.deConverter.fromBoundary(newData);

		existingDevice.addDataToUser(dataEntity);

		this.dataDao.save(dataEntity);
		this.userDao.save(existingDevice);
		return this.deConverter.toBoundary(dataEntity);
	}

	@Override
	public DataBoundary updateData(DataBoundary update) {
		utils.assertNull(update);
		utils.assertNull(update.getDataId());

		Optional<DataEntity> existingData = dataDao.findById(update.getDataId());
		if (existingData.isPresent()) {
			DataEntity dataEntity = existingData.get();
			if (update.getDataAttributes() != null) {
				dataEntity.setDataAttributes(jsConverter.mapToJSON(update.getDataAttributes()));
			}
			if (update.getDataType() != null) {
				dataEntity.setDataType(update.getDataType().name());
			}
			dataEntity = this.dataDao.save(dataEntity);
			return this.deConverter.toBoundary(dataEntity);
		} else {
			throw new NotFoundException("Could not find data in the database");
		}
	}

	@Override
	@Transactional
	public DataBoundary deleteData(String deviceId, String dataId) {
		utils.assertNull(deviceId);
		utils.assertNull(dataId);

		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();		
		UserEntity existingDevice = userDao.findById(deviceId)
				.orElseThrow(() -> new NotFoundException("Device not found: " + deviceId));
		
		utils.assertOwnership(existingDevice.getDeviceOwner().getUid(), authenticatedUser);

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
		utils.assertNull(deviceId);

		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();		
		UserEntity existingDevice = userDao.findById(deviceId)
				.orElseThrow(() -> new NotFoundException("Device not found: " + deviceId));

		utils.assertOwnership(existingDevice.getDeviceOwner().getUid(), authenticatedUser);
		this.dataDao.deleteAll(existingDevice.getUserData());
	}

	@Override
	@Transactional(readOnly = true)
	public DataBoundary getSpecificData(String deviceId, String dataId) {
		utils.assertNull(deviceId);
		utils.assertNull(dataId);

		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();		
		
		// Get only users of role DEVICE	
		Optional<UserEntity> existingDevice = userDao.findByUidAndRole(deviceId, UserRole.DEVICE.name());
		UserEntity deviceEntity;
		if (existingDevice.isPresent()) {
			 deviceEntity = existingDevice.get();
		}
		else throw new NotFoundException("Device not found: " + deviceId);

		// check if device owned by the authenticated user
		utils.assertOwnership(authenticatedUser, deviceEntity.getDeviceOwner().getUid());

		Optional<DataEntity> existingData = this.dataDao.findById(dataId);
		DataEntity dataEntity;
		if (existingData.isPresent()) {
			 dataEntity = existingData.get();
		}
		else throw new NotFoundException("Could not find data by id " + dataId);
		
		// Check if data owned by the device id
		utils.assertOwnership(deviceEntity.getUid(), dataEntity.getDataOwner().getUid());

		return this.deConverter.toBoundary(dataEntity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DataBoundary> getAllData(String deviceId, int page, int size) {
		utils.assertNull(deviceId);

		String authenticatedUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();		
		
		Optional<UserEntity> existingDevice = userDao.findByUidAndRole(deviceId, UserRole.DEVICE.name());
		UserEntity deviceEntity;
		if (existingDevice.isPresent()) {
			 deviceEntity = existingDevice.get();
		}
		else throw new NotFoundException("Device not found: " + deviceId);

		utils.assertOwnership(authenticatedUser, deviceEntity.getDeviceOwner().getUid());

		return this.dataDao.findAll(PageRequest.of(page, size, Direction.DESC, "createdTimestamp", "dataId"))
				.getContent().stream().map(this.deConverter::toBoundary).collect(Collectors.toList());
	}
}