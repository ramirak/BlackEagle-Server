package com.framework.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.framework.boundaries.DataBoundary;
import com.framework.logic.DataService;

@RestController
public class DataController {
	private DataService dataService;

	@Autowired
	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	@RequestMapping(path = "/data/add/{deviceId}",
			method = RequestMethod.POST, 
			produces = MediaType.APPLICATION_JSON_VALUE, 
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public DataBoundary addData(@RequestBody DataBoundary newData, @PathVariable("deviceId") String deviceId) {
		return dataService.addData(deviceId, newData);
	}

	@RequestMapping(path = "/data/update",
			method = RequestMethod.PUT,
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public DataBoundary updateData(@RequestBody DataBoundary newDetails) {
		return dataService.updateData(newDetails);
	}

	@RequestMapping(path = "/data/delete/{deviceId}/{dataId}",
			method = RequestMethod.DELETE)
	public DataBoundary deleteData(@PathVariable("deviceId") String deviceId, @PathVariable("dataId") String dataId) {
		return dataService.deleteData(deviceId, dataId);
	}

	@RequestMapping(path = "/data/deleteAll/{deviceId}",
			method = RequestMethod.DELETE)
	public void deleteAllData(@RequestBody String deviceId) {
		dataService.deleteAllData(deviceId);
	}

	@RequestMapping(path = "/data/get/{deviceId}/{dataId}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public DataBoundary getSpecificData(@PathVariable("deviceId") String deviceId, @PathVariable("dataId") String dataId) {
		return dataService.getSpecificData(deviceId, dataId);
	}

	@RequestMapping(path = "/data/get/{deviceId}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public DataBoundary[] getAllData(@PathVariable("deviceId") String deviceId,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "size", required = false, defaultValue = "5") int size) {
		return dataService.getAllData(deviceId, page, size).toArray(new DataBoundary[0]);
	}

}