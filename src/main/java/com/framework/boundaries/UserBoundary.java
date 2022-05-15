package com.framework.boundaries;

import com.framework.constants.UserRole;

public class UserBoundary {
	private UserIdBoundary userId;
	private UserRole role;
	private String name;
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

	public UserBoundary getOwner() {
		return owner;
	}

	public void setOwner(UserBoundary owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
