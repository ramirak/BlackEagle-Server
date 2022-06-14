package com.framework.logic.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.framework.boundaries.DataBoundary;
import com.framework.constants.UserData;
import com.framework.data.DataEntity;

@Component
public class DataEntityConverterImplementation implements EntityConverter<DataEntity, DataBoundary> {
	private JsonConverter jsConverter;

	@Autowired
	public void setJsConverter(JsonConverter jsConverter) {
		this.jsConverter = jsConverter;
	}

	@Override
	public DataBoundary toBoundary(DataEntity entity) {
		DataBoundary dataBoundary = new DataBoundary();
		dataBoundary.setCreatedTimestamp(entity.getCreatedTimestamp());
		dataBoundary.setDataAttributes(jsConverter.JSONToMap(entity.getDataAttributes()));
		dataBoundary.setDataId(entity.getId());
		dataBoundary.setDataType(UserData.valueOf(entity.getDataType()));
		return dataBoundary;
	}

	@Override
	public DataEntity fromBoundary(DataBoundary boundary) {
		DataEntity dataEntity = new DataEntity();
		dataEntity.setCreatedTimestamp(boundary.getCreatedTimestamp());
		dataEntity.setDataAttributes(jsConverter.mapToJSON(boundary.getDataAttributes()));
		dataEntity.setDataId(boundary.getDataId());
		dataEntity.setDataType(boundary.getDataType().name());
		return dataEntity;
	}
}
