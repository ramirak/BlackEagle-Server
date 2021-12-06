package com.framework.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.framework.boundaries.UserBoundary;
import com.framework.logic.DeviceService;

public class DeviceController {
	//TODO: getAll and specific
	private DeviceService deviceService;

	@Autowired
	public void setDeviceService(DeviceService deviceService) {
		this.deviceService = deviceService;
	}

	@RequestMapping(path = "/device/add/{userId}",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary addDevice(@RequestBody UserBoundary device, @PathVariable("userId") String userId) {
		return deviceService.addDevice(device);
	}

	@RequestMapping(path = "/device/update",
			method = RequestMethod.PUT,
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary updateDevice(@RequestBody UserBoundary device) {
		return deviceService.updateDevice(device);
	}

	@RequestMapping(path = "/device/delete/{userId}/{deviceId}",
			method = RequestMethod.DELETE)
	public UserBoundary deleteDevice(@PathVariable("userId") String userId, @PathVariable String deviceId) {
		return deviceService.deleteDevice(deviceId);
	}
}
