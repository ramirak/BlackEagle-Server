package com.framework.data.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.framework.data.PasswordEntity;

public interface PasswordDao extends PagingAndSortingRepository<PasswordEntity, String>{

}
