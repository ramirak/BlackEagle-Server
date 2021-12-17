package com.framework.data.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import com.framework.data.DataEntity;
import com.framework.data.UserEntity;
import com.framework.datatypes.UserData;

public interface DataDao extends PagingAndSortingRepository<DataEntity, String> {

	public List<DataEntity> findAllByDataOwnerUidAndDataType(@Param("uid") UserEntity dataOwner, UserData type,
			Pageable pageable);

	public List<DataEntity> findAllByDataOwnerUidAndDataTypeAndCreatedTimestamp(@Param("uid") UserEntity dataOwner,
			UserData type, Date createdTimestamp, Pageable pageable);

}