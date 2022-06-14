package com.framework.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.framework.boundaries.UserBoundary;
import com.framework.logic.DeviceService;

@RestController
public class DeviceController {
	private DeviceService deviceService;

	@Autowired
	public void setDeviceService(DeviceService deviceService) {
		this.deviceService = deviceService;
	}

	@RequestMapping(path = "/device/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary addDevice(@RequestBody UserBoundary device) {
		return deviceService.addDevice(device);
	}

	@RequestMapping(path = "/device/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary updateDevice(@RequestBody UserBoundary device) {
		return deviceService.updateDevice(device);
	}

	@RequestMapping(path = "/device/delete/{deviceId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary deleteDevice(@PathVariable String deviceId) {
		return deviceService.deleteDevice(deviceId);
	}

	@RequestMapping(path = "/device/get/{deviceId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary getSpecificDevice(@PathVariable String deviceId) {
		return deviceService.getSpecificDevice(deviceId);
	}

	@RequestMapping(path = "/device/getAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary[] getAllDevices(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "size", required = false, defaultValue = "5") int size) {
		return deviceService.getAllDevices(page, size).toArray(new UserBoundary[0]);
	}
}
