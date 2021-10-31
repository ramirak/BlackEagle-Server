package com.framework.logic;

import java.util.List;

import com.framework.boundaries.UserBoundary;

public interface DeviceService {
	public UserBoundary addDevice(String userId, UserBoundary device);

	public UserBoundary updateDevice(UserBoundary device); 
	
	public UserBoundary deleteDevice(String userId, String deviceID);
	
	public UserBoundary getSpecificDevice(String deviceID); 
	
	public List<UserBoundary> getAllDevices(); 


	
}
