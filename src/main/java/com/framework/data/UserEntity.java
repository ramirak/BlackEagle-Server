package com.framework.data;

import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Users")
public class UserEntity {
	@Id
	private String uid;
	private String username;
	private String role;
	private int deviceCount;
	private boolean active;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity deviceOwner;

	@OneToMany(mappedBy = "deviceOwner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<UserEntity> devices;

	@OneToMany(mappedBy = "dataOwner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<DataEntity> userData;

	@OneToMany(mappedBy = "passOwner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<PasswordEntity> passwords;

	public UserEntity() {
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int getDeviceCount() {
		return deviceCount;
	}

	public void setDeviceCount(int deviceCount) {
		this.deviceCount = deviceCount;
	}

	public UserEntity getDeviceOwner() {
		return deviceOwner;
	}

	public void setDeviceOwner(UserEntity deviceOwner) {
		this.deviceOwner = deviceOwner;
	}

	public Set<UserEntity> getDevices() {
		return devices;
	}

	public void setDevices(Set<UserEntity> devices) {
		this.devices = devices;
	}

	public Set<DataEntity> getUserData() {
		return userData;
	}

	public void setUserData(Set<DataEntity> userData) {
		this.userData = userData;
	}

	public Set<PasswordEntity> getPasswords() {
		return passwords;
	}

	public void setPasswords(Set<PasswordEntity> passwords) {
		this.passwords = passwords;
	}

	public void addPassword(PasswordEntity pe) {
		this.passwords.add(pe);
		pe.setOwner(this);
	}
	
	public PasswordEntity getActivePasswordEntity() {
		for (PasswordEntity pe : this.passwords) {
			if(pe.getActive())
				return pe;
		}
		return null;
	}
	
	public boolean isPasswordInHistory(String hashedPass) {
		for (PasswordEntity pe : this.passwords) {
			if(pe.getPassword().equals(hashedPass))
				return true;
		}
		return false;
	}
	
	public void addDataToUser(DataEntity dataEntity) {
		this.userData.add(dataEntity);
		dataEntity.setDataOwner(this);	
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(deviceCount, devices, uid, passwords, role, userData);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserEntity other = (UserEntity) obj;
		return deviceCount == other.deviceCount && Objects.equals(devices, other.devices)
				&& Objects.equals(uid, other.uid) && Objects.equals(passwords, other.passwords)
				&& Objects.equals(role, other.role) && Objects.equals(userData, other.userData);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
