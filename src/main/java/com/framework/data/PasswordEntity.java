package com.framework.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Passwords")
public class PasswordEntity implements Serializable{

	private static final long serialVersionUID = 1L;
	private String password;
	private Date creationTime;
	private boolean active;
	private String hint;
	
	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity passOwner;
	
	public UserEntity getOwner() {
		return passOwner;
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
		return Objects.hash(active, creationTime, passOwner, password);
	}
	public String getHint() {
		return hint;
	}
	public void setHint(String hint) {
		this.hint = hint;
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
		return active == other.active && Objects.equals(creationTime, other.creationTime)
				&& Objects.equals(passOwner, other.passOwner) && Objects.equals(password, other.password);
	}

}
