package com.framework.boundaries;

public class UserIdBoundary {
	private String uid;
	private PasswordBoundary password;

	public UserIdBoundary() {
		super();
	}

	public UserIdBoundary(String uid, PasswordBoundary password) {
		this.uid = uid;
		this.password = password;
	}

	public String getUID() {
		return uid;
	}

	public void setUID(String uid) {
		this.uid = uid;
	}

	public PasswordBoundary getPasswordBoundary() {
		return password;
	}

	public void setPassword(PasswordBoundary password) {
		this.password = password;
	}
}
