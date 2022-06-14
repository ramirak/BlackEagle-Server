package com.framework.logic;

import java.util.List;

import com.framework.boundaries.UserBoundary;

public interface DeviceService {

	public UserBoundary addDevice(UserBoundary device);

	public UserBoundary updateDevice(UserBoundary update);

	public UserBoundary deleteDevice(String deviceId);

	public UserBoundary getSpecificDevice(String deviceId);

	public List<UserBoundary> getAllDevices(int page, int size);
}
