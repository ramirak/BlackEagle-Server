package com.framework.logic;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.framework.boundaries.DataBoundary;
import com.framework.constants.UserData;

public interface DataService {


	public DataBoundary addData(String ownerId, DataBoundary newData, MultipartFile file);

	public DataBoundary updateData(DataBoundary update);
	
	public DataBoundary deleteData(String deviceId, String dataId);
	
	public void deleteAllData(String deviceId); 
	
	public DataBoundary getSpecificData(String deviceId, String dataId);
	
	public List<DataBoundary> getAllData(String deviceId,UserData type, int page, int size);
}
