package com.framework.boundaries;

import com.framework.datatypes.UserRole;

public class UserBoundary {
	private UserIdBoundary userId;
	private UserRole role;
	private String username;
	private Boolean active;
	private Integer deviceCount;
	private UserBoundary owner;
	
	public UserBoundary() {
		super();
	}
	public UserRole getRole() {
		return role;
	}
	public void setRole(UserRole role) {
		this.role = role;
	}
	public UserIdBoundary getUserId() {
		return userId;
	}
	public void setUserId(UserIdBoundary userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public Integer getDeviceCount() {
		return deviceCount;
	}
	public void setDeviceCount(int deviceCount) {
		this.deviceCount = deviceCount;
	}
	public UserBoundary getOwner() {
		return owner;
	}
	public void setOwner(UserBoundary owner) {
		this.owner = owner;
	} 
}
