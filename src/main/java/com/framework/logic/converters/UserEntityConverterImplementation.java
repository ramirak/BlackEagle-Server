package com.framework.logic.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.framework.boundaries.PasswordBoundary;
import com.framework.boundaries.UserBoundary;
import com.framework.boundaries.UserIdBoundary;
import com.framework.constants.UserRole;
import com.framework.data.UserEntity;

@Component
public class UserEntityConverterImplementation implements EntityConverter<UserEntity, UserBoundary> {
	private PasswordEntityConverterImlementation peConverter;

	@Autowired
	public void setPeConverter(PasswordEntityConverterImlementation peConverter) {
		this.peConverter = peConverter;
	}

	@Override
	public UserBoundary toBoundary(UserEntity entity) {
		UserBoundary userBoundary = new UserBoundary();
		userBoundary.setRole(UserRole.valueOf(entity.getRole()));
		userBoundary.setName(entity.getName());
		PasswordBoundary pb = peConverter.toBoundary(entity.getActivePasswordEntity());
		UserIdBoundary uidBoundary = new UserIdBoundary(entity.getId(), pb);
		userBoundary.setUserId(uidBoundary);
		return userBoundary;
	}

	@Override
	public UserEntity fromBoundary(UserBoundary boundary) {
		UserEntity userEntity = new UserEntity();
		userEntity.setRole(boundary.getRole().name());
		userEntity.setName(boundary.getName());
		userEntity.setUid(boundary.getUserId().getUID());
		return userEntity;
	}
}
