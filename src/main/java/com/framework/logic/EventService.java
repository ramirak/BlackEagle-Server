package com.framework.logic;

import com.framework.constants.EventType;
import com.framework.data.EventEntity;

public interface EventService {

	public EventEntity createEvent(String creator, EventType eventType);
}
