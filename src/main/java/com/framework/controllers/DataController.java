package com.framework.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.framework.boundaries.DataBoundary;
import com.framework.constants.UserData;
import com.framework.logic.DataService;

@RestController
public class DataController {
	private DataService dataService;

	@Autowired
	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	@RequestMapping(path = "/data/add/{ownerId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public DataBoundary addData(@PathVariable("ownerId") String ownerId, @RequestBody DataBoundary newData) {
		return dataService.addData(ownerId, newData);
	}

	@RequestMapping(path = "/data/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DataBoundary upload(@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestPart DataBoundary newData) {
		return dataService.addData(newData, file);
	}

	@RequestMapping(path = "/data/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public DataBoundary updateData(@RequestBody DataBoundary newDetails) {
		return dataService.updateData(newDetails);
	}

	@RequestMapping(path = "/data/delete/{dataId}", method = RequestMethod.DELETE)
	public DataBoundary deleteData(@PathVariable("dataId") String dataId) {
		return dataService.deleteData(dataId);
	}

	@RequestMapping(path = "/data/deleteAll/{ownerId}/{dataType}", method = RequestMethod.DELETE)
	public void deleteAllData(@PathVariable("ownerId") String ownerId, @PathVariable("dataType") String dataType) {
		dataService.deleteAllData(ownerId, dataType);
	}

	@RequestMapping(path = "/data/get/{deviceId}/{dataId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public DataBoundary getSpecificData(@PathVariable("deviceId") String deviceId,
			@PathVariable("dataId") String dataId) {
		return dataService.getSpecificData(deviceId, dataId);
	}

	@RequestMapping(path = "/data/getAll/{deviceId}/{dataType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public DataBoundary[] getAllData(@PathVariable("deviceId") String deviceId, @PathVariable("dataType") UserData type,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "size", required = false, defaultValue = "5") int size) {
		return dataService.getAllData(deviceId, type, page, size).toArray(new DataBoundary[0]);
	}

}
