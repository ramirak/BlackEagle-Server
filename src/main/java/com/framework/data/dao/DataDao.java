package com.framework.data.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import com.framework.data.DataEntity;

public interface DataDao extends PagingAndSortingRepository<DataEntity, String> {

	public DataEntity findByDataIdAndDataOwnerUid(@Param("uid") String dataUid, String ownerUid);

	public List<DataEntity> findAllByDataTypeAndDataOwnerUid(String type, String uid, Pageable pageable);

	public List<DataEntity> findAllByDataTypeAndDataOwnerUid(String type, String uid);

	public List<DataEntity> findAllByDataOwnerUid(String uid);

	public void deleteAllByDataTypeAndDataOwnerUid(String type, String uid);
}