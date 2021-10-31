package com.framework.data.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.framework.data.UserEntity;
import com.framework.datatypes.UserRole;

public interface UserDao extends PagingAndSortingRepository<UserEntity, String> {
	public List<UserEntity> findAllByActiveAndRole(@Param("active") boolean active, UserRole role, Pageable pageable);
}
