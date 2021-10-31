package com.framework.logic;

import java.util.List;

import com.framework.boundaries.DataBoundary;

public interface DataService {
	public DataBoundary addData(String deviceId, DataBoundary newData);
	
	public DataBoundary updateData(DataBoundary update);
	
	public DataBoundary deleteData(String deviceId, String dataId);
	
	public void deleteAllData(String deviceId); 
	
	public DataBoundary getSpecificData(String deviceId, String dataId);
	
	public List<DataBoundary> getAllData(String deviceId, int page, int size);
}
