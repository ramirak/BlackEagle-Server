package com.framework.data.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.framework.data.DataEntity;

public interface DataDao extends PagingAndSortingRepository<DataEntity, String>{

	public List<DataEntity> findAllByActive(
			@Param("active") boolean active,
			Pageable pageable);
	
}