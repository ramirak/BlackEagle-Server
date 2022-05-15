package com.framework.data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.SortNatural;
import org.springframework.data.domain.Persistable;

import com.framework.constants.PasswordsDefaults;

@Entity
@Table(name = "Users")
public class UserEntity implements Persistable<String> {
	@Id
	private String uid;

	@Transient // Save -> update / create
	private boolean update; // If true -> not new entity, If false, create a new entity

	private String role;
	private String name;

	@ManyToOne
	@JoinColumn(name = "OwnerUid")
	private UserEntity deviceOwner;

	@OneToMany(mappedBy = "deviceOwner", cascade = CascadeType.ALL)
	private Set<UserEntity> devices;

	@OneToMany(mappedBy = "dataOwner", cascade = CascadeType.REMOVE)
	private Set<DataEntity> userData;

	@SortNatural
	@ElementCollection
	@OneToMany(mappedBy = "passOwner", cascade = CascadeType.ALL)
	private SortedSet<PasswordEntity> passwords;

	@OneToMany(mappedBy = "eventOwner", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<EventEntity> events;

	public UserEntity() {
		// Passwords are ordered by date
		this.passwords = new TreeSet<>();
		this.userData = new HashSet<>();
		this.devices = new HashSet<>();
	}

	@Override
	public String getId() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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

	public Set<EventEntity> getEvents() {
		return events;
	}

	public void setEvents(Set<EventEntity> events) {
		this.events = events;
	}

	public Set<PasswordEntity> getPasswords() {
		return passwords;
	}

	public void setPasswords(TreeSet<PasswordEntity> passwords) {
		this.passwords = passwords;
	}

	public Optional<PasswordEntity> addPassword(PasswordEntity pe) {
		Optional<PasswordEntity> firstElement = Optional.empty();

		if (passwords.size() >= PasswordsDefaults.HISTORY && PasswordsDefaults.HISTORY > 1) {
			firstElement = passwords.stream().findFirst();
			passwords.remove(firstElement.get());
		}
		if (passwords.size() > 0)
			getActivePasswordEntity().setActive(false);
		pe.setActive(true);
		this.passwords.add(pe);
		pe.setOwner(this);
		return firstElement;
	}

	public PasswordEntity getActivePasswordEntity() {
		for (PasswordEntity pe : this.passwords) {
			if (pe.getActive())
				return pe;
		}
		return null;
	}

	public void addDataToUser(DataEntity dataEntity) {
		this.userData.add(dataEntity);
		dataEntity.setDataOwner(this);
	}

	public void addDeviceToUser(UserEntity deviceEntity) {
		this.devices.add(deviceEntity);
		deviceEntity.setDeviceOwner(this);
	}

	public void addEventToUser(EventEntity eventEntity) {
		this.events.add(eventEntity);
		eventEntity.setEventOwner(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(deviceOwner, role, name, uid);
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
		return Objects.equals(deviceOwner, other.deviceOwner) && Objects.equals(devices, other.devices)
				&& Objects.equals(passwords, other.passwords) && Objects.equals(events, other.events)
				&& Objects.equals(role, other.role) && Objects.equals(name, other.name)
				&& Objects.equals(uid, other.uid) && Objects.equals(userData, other.userData);
	}

	public boolean isUpdate() {
		return this.update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	@Override
	public boolean isNew() {
		return !this.update;
	}

	@PreRemove
	@PostLoad
	void markUpdated() {
		this.update = true;
	}
}
