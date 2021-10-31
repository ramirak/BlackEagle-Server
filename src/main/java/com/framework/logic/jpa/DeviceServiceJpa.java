package com.framework.logic.jpa;

import java.util.List;

import org.springframework.stereotype.Service;

import com.framework.boundaries.UserBoundary;
import com.framework.data.dao.UserDao;
import com.framework.logic.DeviceService;
import com.framework.logic.converters.UserEntityConverterImplementation;

@Service
public class DeviceServiceJpa implements DeviceService {
	private UserDao userDao;
	private UserEntityConverterImplementation ueConverter;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void setUeConverter(UserEntityConverterImplementation ueConverter) {
		this.ueConverter = ueConverter;
	}

	@Override
	public UserBoundary addDevice(String userId, UserBoundary device) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserBoundary updateDevice(UserBoundary device) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserBoundary deleteDevice(String userId, String deviceID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserBoundary getSpecificDevice(String deviceID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserBoundary> getAllDevices() {
		// TODO Auto-generated method stub
		return null;
	}

}
