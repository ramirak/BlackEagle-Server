package com.framework.logic;

import java.util.List;

import com.framework.boundaries.EventBoundary;
import com.framework.constants.EventType;
import com.framework.data.EventEntity;

public interface EventService {

	public EventEntity createEvent(String creator, EventType eventType);

	public List<EventBoundary> getAllData(int page, int size);
}
