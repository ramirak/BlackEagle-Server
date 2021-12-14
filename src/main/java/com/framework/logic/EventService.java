package com.framework.logic;

import com.framework.data.EventEntity;

public interface EventService {
	public EventEntity createEvent(String creator, EventEntity event);
}
