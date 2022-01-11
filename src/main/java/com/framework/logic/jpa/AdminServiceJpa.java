package com.framework.logic.jpa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.framework.boundaries.UserBoundary;
import com.framework.constants.UserRole;
import com.framework.data.UserEntity;
import com.framework.data.dao.PasswordDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.AdminService;
import com.framework.logic.converters.UserEntityConverterImplementation;
import com.framework.utilities.Validations;

@Service
public class AdminServiceJpa implements AdminService {
	private UserDao userDao;
	private PasswordDao passwordDao;
	private UserEntityConverterImplementation ueConverter;
	private Validations utils;

	public AdminServiceJpa() {
	}

	@Autowired
	public void setUtils(Validations utils) {
		this.utils = utils;
	}

	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Autowired
	public void setPasswordDao(PasswordDao passwordDao) {
		this.passwordDao = passwordDao;
	}

	@Autowired
	public void setUeConverter(UserEntityConverterImplementation ueConverter) {
		this.ueConverter = ueConverter;
	}
	@Override
	public UserBoundary designateUser(UserBoundary user) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public UserBoundary getSpecificUser(String email) {
		// TODO Check if current role is Admin
		utils.assertAuthorizedOperation("ADMIN");

		Optional<UserEntity> existingUser = userDao.findById(email);
		if (existingUser.isPresent())
			return ueConverter.toBoundary(existingUser.get());
		throw new NotFoundException("User does not exists in the database");
	}

	// Retrieve only real users and not devices
	@Override
	public List<UserBoundary> getAllUsers(int page, int size) {
		// TODO Check if current role is Admin
		utils.assertAuthorizedOperation("ADMIN");
		return this.userDao
				.findAllByActiveAndRole(true, UserRole.PLAYER, PageRequest.of(page, size, Direction.DESC, "username"))
				.stream().map(this.ueConverter::toBoundary).collect(Collectors.toList());
	}

	@Override
	public UserBoundary resetPassword(String email) {
		// TODO Check if current role is Admin
		utils.assertAuthorizedOperation("ADMIN");

		Optional<UserEntity> existingUser = userDao.findById(email);
		if (existingUser.isPresent()) {
			// TODO Send mail with reset link
		}
		throw new NotFoundException("User does not exists in the database");
	}

	@Override
	public UserBoundary deleteAccount(String email) {
		// TODO Check if current role is Admin
		utils.assertAuthorizedOperation("ADMIN");

		Optional<UserEntity> existingUser = userDao.findById(email);
		if (existingUser.isPresent()) {
			passwordDao.deleteById(email);
			userDao.deleteById(email);
		}
		throw new NotFoundException("User does not exists in the database");
	}
}
