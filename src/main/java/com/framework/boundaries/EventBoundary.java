package com.framework.boundaries;

import java.util.Date;
import java.util.Map;

import com.framework.constants.EventType;

public class EventBoundary {
	private Integer id;
	private EventType type;
	private Date timeOfEvent;
	private Map<String, Object> eventAttributes;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public Date getTimeOfEvent() {
		return timeOfEvent;
	}

	public void setTimeOfEvent(Date timeOfEvent) {
		this.timeOfEvent = timeOfEvent;
	}

	public Map<String, Object> getEventAttributes() {
		return eventAttributes;
	}

	public void setEventAttributes(Map<String, Object> eventAttributes) {
		this.eventAttributes = eventAttributes;
	}
}
