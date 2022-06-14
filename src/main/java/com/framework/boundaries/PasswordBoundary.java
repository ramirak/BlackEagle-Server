package com.framework.boundaries;

import java.util.Date;

public class PasswordBoundary {
	private String password, optionalPassword;
	private Date creationTime;
	private Boolean active;

	public PasswordBoundary() {
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getOptionalPassword() {
		return optionalPassword;
	}

	public void setOptionalPassword(String optionalPassword) {
		this.optionalPassword = optionalPassword;
	}
}
