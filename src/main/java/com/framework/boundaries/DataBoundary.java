package com.framework.boundaries;

import java.util.Date;
import java.util.Map;

import javax.persistence.Lob;

import com.framework.constants.UserData;
import com.framework.data.UserEntity;

public class DataBoundary {
	private String dataId;
	private UserData dataType;
	private Date createdTimestamp;
	private Map<String, Object> dataAttributes;
	private UserEntity dataOwner;

	public DataBoundary() {
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public UserData getDataType() {
		return dataType;
	}

	public void setDataType(UserData dataType) {
		this.dataType = dataType;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	@Lob
	public Map<String, Object> getDataAttributes() {
		return dataAttributes;
	}

	public void setDataAttributes(Map<String, Object> dataAttributes) {
		this.dataAttributes = dataAttributes;
	}

	public UserEntity getDataOwner() {
		return dataOwner;
	}

	public void setDataOwner(UserEntity dataOwner) {
		this.dataOwner = dataOwner;
	}
}
