package com.framework.data.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.framework.data.EventEntity;


public interface EventDao extends PagingAndSortingRepository<EventEntity, String>{

}
