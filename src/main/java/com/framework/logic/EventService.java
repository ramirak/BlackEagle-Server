package com.framework.logic;

import com.framework.data.EventEntity;
import com.framework.datatypes.EventType;

public interface EventService {

	public EventEntity createEvent(String creator, EventType eventType);
}
