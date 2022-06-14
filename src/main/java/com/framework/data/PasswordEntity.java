package com.framework.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "Passwords")
public class PasswordEntity implements Serializable, Comparable<PasswordEntity> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Incremental id
	private int id;

	private static final long serialVersionUID = 1L;
	private String password;

	@Temporal(TemporalType.TIMESTAMP)
	private Date creationTime;
	private boolean active;

	@ManyToOne
	@JoinColumn(name = "pass_owner")
	private UserEntity passOwner;

	public UserEntity getOwner() {
		return passOwner;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setOwner(UserEntity passOwner) {
		this.passOwner = passOwner;
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

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		return Objects.hash(active, creationTime, id, passOwner, password);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PasswordEntity other = (PasswordEntity) obj;
		return active == other.active && Objects.equals(creationTime, other.creationTime) && id == other.id
				&& Objects.equals(passOwner, other.passOwner) && Objects.equals(password, other.password);
	}

	@Override
	public int compareTo(PasswordEntity o) {
		return this.creationTime.compareTo(o.creationTime);
	}
}
