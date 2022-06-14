package com.framework.data;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "Events")
public class EventEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Incremental id
	private int id;
	private String type;
	private Date timeOfEvent;
	private String eventAttributes;

	@ManyToOne
	@JoinColumn(name = "event_owner")
	private UserEntity eventOwner;

	public EventEntity() {
	}

	public EventEntity(String type, Date timeOfEvent) {
		this.type = type;
		this.timeOfEvent = timeOfEvent;
	}

	public EventEntity(String type, Date timeOfEvent, String eventAttributes) {
		this.type = type;
		this.timeOfEvent = timeOfEvent;
		this.eventAttributes = eventAttributes;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getTimeOfEvent() {
		return timeOfEvent;
	}

	public void setTimeOfEvent(Date timeOfEvent) {
		this.timeOfEvent = timeOfEvent;
	}

	@Lob
	public String getEventAttributes() {
		return eventAttributes;
	}

	public void setEventAttributes(String eventAttributes) {
		this.eventAttributes = eventAttributes;
	}

	public UserEntity getEventOwner() {
		return eventOwner;
	}

	public void setEventOwner(UserEntity eventOwner) {
		this.eventOwner = eventOwner;
	}

	@Override
	public int hashCode() {
		return Objects.hash(eventOwner, id, timeOfEvent, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventEntity other = (EventEntity) obj;
		return Objects.equals(eventAttributes, other.eventAttributes) && Objects.equals(eventOwner, other.eventOwner)
				&& id == other.id && Objects.equals(timeOfEvent, other.timeOfEvent) && type == other.type;
	}
}
