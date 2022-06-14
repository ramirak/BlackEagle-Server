package com.framework.logic.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.framework.boundaries.EventBoundary;
import com.framework.constants.EventType;
import com.framework.data.EventEntity;

@Component
public class EventEntityConverterImplementation implements EntityConverter<EventEntity, EventBoundary> {
	private JsonConverter jsConverter;

	@Autowired
	public void setJsConverter(JsonConverter jsConverter) {
		this.jsConverter = jsConverter;
	}

	@Override
	public EventBoundary toBoundary(EventEntity entity) {
		EventBoundary eb = new EventBoundary();
		eb.setType(EventType.valueOf(entity.getType()));
		eb.setTimeOfEvent(entity.getTimeOfEvent());
		eb.setEventAttributes(jsConverter.JSONToMap(entity.getEventAttributes()));
		return eb;
	}

	@Override
	public EventEntity fromBoundary(EventBoundary boundary) {
		EventEntity entity = new EventEntity();
		entity.setType(boundary.getType().name());
		entity.setTimeOfEvent(boundary.getTimeOfEvent());
		entity.setEventAttributes(jsConverter.mapToJSON(boundary.getEventAttributes()));
		return entity;
	}
}
