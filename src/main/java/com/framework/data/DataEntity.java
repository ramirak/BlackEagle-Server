package com.framework.data;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "Data")
public class DataEntity implements Persistable<String>{
	@Id
	private String dataId;
	private String dataType;
	private Date createdTimestamp;
	private String dataAttributes;
	
	@Transient
	private boolean update;
	
	@ManyToOne
	@JoinColumn(name = "data_owner")
	private UserEntity dataOwner;
	
	public DataEntity() {
	}
	
	@Override
	public String getId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
	
	@Lob
	public String getDataAttributes() {
		return dataAttributes;
	}

	public void setDataAttributes(String dataAttributes) {
		this.dataAttributes = dataAttributes;
	}

	public UserEntity getDataOwner() {
		return dataOwner;
	}

	public void setDataOwner(UserEntity dataOwner) {
		this.dataOwner = dataOwner;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdTimestamp, dataId, dataOwner, dataType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataEntity other = (DataEntity) obj;
		return Objects.equals(createdTimestamp, other.createdTimestamp)
				&& Objects.equals(dataAttributes, other.dataAttributes) && Objects.equals(dataId, other.dataId)
				&& Objects.equals(dataOwner, other.dataOwner) && Objects.equals(dataType, other.dataType);
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

