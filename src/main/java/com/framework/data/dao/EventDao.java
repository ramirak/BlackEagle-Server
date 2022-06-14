package com.framework.data.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import com.framework.constants.EventType;
import com.framework.data.EventEntity;

public interface EventDao extends PagingAndSortingRepository<EventEntity, String> {

	public List<EventEntity> findAllByTypeAndEventOwnerUid(EventType type, String ownerUid, Pageable pageable);

	public List<EventEntity> findAllByEventOwnerUid(String ownerUid, Pageable pageable);
}
