package com.framework.data.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.framework.constants.UserData;
import com.framework.data.DataEntity;
import com.framework.data.UserEntity;

public interface DataDao extends PagingAndSortingRepository<DataEntity, String> {

	public DataEntity findByDataIdAndDataOwnerUid(@Param("uid") String dataUid, String ownerUid);

	public List<DataEntity> findAllByDataOwnerUidAndDataType(@Param("uid") String uid, UserData type,
			Pageable pageable);

}