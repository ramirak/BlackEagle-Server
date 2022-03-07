package com.framework.data.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.framework.constants.UserRole;
import com.framework.data.UserEntity;

public interface UserDao extends PagingAndSortingRepository<UserEntity, String> {

	public List<UserEntity> findAllByActiveAndRoleAndDeviceOwnerUid(@Param("active") boolean active, UserRole role,
			String ownerUid, Pageable pageable);

	public Optional<UserEntity> findByActiveAndUidAndRoleAndDeviceOwnerUid(@Param("active") boolean active, String uid,
			UserRole role, String ownerUid);
}
