package com.framework.data.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.framework.constants.EventType;
import com.framework.data.EventEntity;

public interface EventDao extends PagingAndSortingRepository<EventEntity, String> {
	
	public List<EventEntity> findAllByType(EventType type, Pageable pageable);
	
	public List<EventEntity> findAllByTypeAndTimeOfEvent(EventType type, Date date,Pageable pageable);
	
	public List<EventEntity> findAllByTimeOfEvent(@Param("timeOfEvent") Date date, Pageable pageable);
	
}
