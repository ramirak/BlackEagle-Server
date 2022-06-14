package com.framework.data.dao;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.framework.data.PasswordEntity;

public interface PasswordDao extends PagingAndSortingRepository<PasswordEntity, String> {
	
	public Optional<PasswordEntity> findByActiveAndPassOwnerUid(@Param("active") boolean active,
			@Param("passOwner") String uid);
}
