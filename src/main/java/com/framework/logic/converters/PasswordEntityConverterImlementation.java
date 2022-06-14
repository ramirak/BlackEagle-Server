package com.framework.logic.converters;

import org.springframework.stereotype.Component;

import com.framework.boundaries.PasswordBoundary;
import com.framework.data.PasswordEntity;

@Component
public class PasswordEntityConverterImlementation implements EntityConverter<PasswordEntity, PasswordBoundary> {

	@Override
	public PasswordBoundary toBoundary(PasswordEntity entity) {
		PasswordBoundary pb = new PasswordBoundary();
		pb.setActive(entity.getActive());
		pb.setCreationTime(entity.getCreationTime());
		pb.setPassword(entity.getPassword());
		return pb;
	}

	@Override
	public PasswordEntity fromBoundary(PasswordBoundary boundary) {
		PasswordEntity pe = new PasswordEntity();
		pe.setActive(boundary.getActive() != null ? boundary.getActive() : true);
		pe.setCreationTime(boundary.getCreationTime());
		pe.setPassword(boundary.getPassword());
		return pe;
	}
}
