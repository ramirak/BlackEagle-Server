package com.framework.data.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.framework.data.DataEntity;

public interface DataDao extends PagingAndSortingRepository<DataEntity, String>{

}