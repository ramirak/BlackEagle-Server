package com.framework.data.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.framework.constants.UserRole;
import com.framework.data.UserEntity;

public interface UserDao extends PagingAndSortingRepository<UserEntity, String> {
	
	public List<UserEntity> findAllByActiveAndRole(
			@Param("active") boolean active,
			UserRole role,
			Pageable pageable);

	public Optional<UserEntity> findByUidAndRole(
			@Param("uid") String uid,
			String role);
	
	public List<UserEntity> findAllByActive(
			@Param("active") boolean active,
			Pageable pageable);
}
