package com.framework.data.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import com.framework.data.UserEntity;

public interface UserDao extends PagingAndSortingRepository<UserEntity, String> {

	public List<UserEntity> findAllByRoleAndDeviceOwnerUid(String role, String ownerUid, Pageable pageable);

	public Optional<UserEntity> findByUidAndRoleAndDeviceOwnerUid(String uid, String role, String ownerUid);
}
